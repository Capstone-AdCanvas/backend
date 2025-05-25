package hello.backend.audio.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MergeService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    @Value("${file.tts-dir}")
    private String ttsDir;

    @Transactional
    public File mergeVideoAudio(List<String> videoUrls, String tema, List<String> ttsUrls) throws IOException {
        String audioFileName = null;

        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "영상 URL이 없습니다.");
        }

        if (tema != null && !tema.isBlank()) {
            audioFileName = switch (tema.toLowerCase()) {
                case "forest" -> "forest.mp3";
                case "water" -> "water.mp3";
                case "wave"  -> "wave.mp3";
                case "fun"   -> "fun.mp3";
                case "sad"   -> "sad.mp3";
                default -> throw new BusinessException(ErrorCode.AUDIO_NOT_FOUND, "요청 정보를 찾을 수 없습니다.");
            };
        }

        String uuid = UUID.randomUUID().toString();
        File tempDir = new File("temp_" + uuid);
        if (!tempDir.exists()) tempDir.mkdirs();

        List<File> downloadedVideos = downloadVideos(videoUrls, tempDir);
        File concatListFile = new File(tempDir, "concat_list.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatListFile))) {
            for (File video : downloadedVideos) {
                writer.write("file '" + video.getAbsolutePath() + "'");
                writer.newLine();
            }
        }

        File mergedVideo = new File(tempDir, "merged.mp4");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegBuilder concatBuilder = new FFmpegBuilder()
                .setInput(concatListFile.getAbsolutePath())
                .addExtraArgs("-f", "concat", "-safe", "0")
                .overrideOutputFiles(true)
                .addOutput(mergedVideo.getAbsolutePath())
                .setFormat("mp4")
                .done();
        executor.createJob(concatBuilder).run();

        FFmpegProbeResult videoInfo = ffprobe.probe(mergedVideo.getAbsolutePath());
        double duration = videoInfo.getFormat().duration;
        File finalOutput = new File("output_" + uuid + ".mp4");

        File intermediateAudio = null;
        if (audioFileName != null) {
            File audioPath = new File("src/main/resources/static/mp3/" + audioFileName);
            File trimmedAudio = new File(tempDir, "trimmed.mp3");

            FFmpegBuilder audioTrimBuilder = new FFmpegBuilder()
                    .setInput(audioPath.getAbsolutePath())
                    .addExtraArgs("-t", String.valueOf(duration))
                    .overrideOutputFiles(true)
                    .addOutput(trimmedAudio.getAbsolutePath())
                    .setFormat("mp3")
                    .done();
            executor.createJob(audioTrimBuilder).run();

            intermediateAudio = trimmedAudio;
        }

        if (ttsUrls != null && !ttsUrls.isEmpty()) {

            File silence = new File("src/main/resources/static/mp3/silence1s.mp3");
            List<File> ttsParts = new ArrayList<>();

            for (String ttsUrl : ttsUrls) {
                File before = new File(tempDir, "silence_before_" + UUID.randomUUID() + ".mp3");
                File tts = new File(tempDir, "tts_" + UUID.randomUUID() + ".mp3");
                File after = new File(tempDir, "silence_after_" + UUID.randomUUID() + ".mp3");

                Files.copy(silence.toPath(), before.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String fileName = Paths.get(ttsUrl).getFileName().toString();
                File source = new File(ttsDir, fileName);

                log.info("💬 TTS URL        = {}", ttsUrl);
                log.info("📁 TTS File name  = {}", fileName);
                log.info("📂 TTS Full path  = {}", source.getAbsolutePath());
                log.info("✅ TTS File exists? {}", source.exists());

                if (!source.exists()) {
                    throw new BusinessException(ErrorCode.AUDIO_NOT_FOUND, "TTS 파일을 찾을 수 없습니다: " + source.getAbsolutePath());
                }

                Files.copy(source.toPath(), tts.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(silence.toPath(), after.toPath(), StandardCopyOption.REPLACE_EXISTING);

                ttsParts.add(before);
                ttsParts.add(tts);
                ttsParts.add(after);
            }

            File concatTtsList = new File(tempDir, "tts_concat.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatTtsList))) {
                for (File f : ttsParts) {
                    writer.write("file '" + f.getAbsolutePath() + "'");
                    writer.newLine();
                }
            }

            File ttsMerged = new File(tempDir, "tts_merged.mp3");
            FFmpegBuilder concatTtsBuilder = new FFmpegBuilder()
                    .setInput(concatTtsList.getAbsolutePath())
                    .addExtraArgs("-f", "concat", "-safe", "0")
                    .overrideOutputFiles(true)
                    .addOutput(ttsMerged.getAbsolutePath())
                    .setFormat("mp3")
                    .done();
            executor.createJob(concatTtsBuilder).run();

            if (intermediateAudio != null) {
                File mixOutput = new File(tempDir, "mixed.mp3");
                FFmpegBuilder mixBuilder = new FFmpegBuilder()
                        .addInput(intermediateAudio.getAbsolutePath())
                        .addInput(ttsMerged.getAbsolutePath())
                        .overrideOutputFiles(true)
                        .addOutput(mixOutput.getAbsolutePath())
                        .addExtraArgs("-filter_complex", "amix=inputs=2:duration=longest")
                        .setFormat("mp3")
                        .done();
                executor.createJob(mixBuilder).run();
                intermediateAudio = mixOutput;
            } else {
                intermediateAudio = ttsMerged;
            }
        }

        if (intermediateAudio != null) {
            FFmpegBuilder mergeBuilder = new FFmpegBuilder()
                    .addInput(mergedVideo.getAbsolutePath())
                    .addInput(intermediateAudio.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(finalOutput.getAbsolutePath())
                    .setVideoCodec("copy")
                    .setAudioCodec("aac")
                    .setFormat("mp4")
                    .addExtraArgs("-map", "0:v:0")
                    .addExtraArgs("-map", "1:a:0")
                    .done();
            executor.createJob(mergeBuilder).run();
        } else {
            Files.copy(mergedVideo.toPath(), finalOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return finalOutput;
    }



    private List<File> downloadVideos(List<String> videoUrls, File saveDir) throws IOException {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < videoUrls.size(); i++) {
            File out = new File(saveDir, "video" + i + ".mp4");
            try (InputStream in = new URL(videoUrls.get(i)).openStream()) {
                Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            files.add(out);
        }
        return files;
    }
}
