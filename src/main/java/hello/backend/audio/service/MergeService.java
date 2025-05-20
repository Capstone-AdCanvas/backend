package hello.backend.audio.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MergeService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    @Transactional
    public File mergeVideoAudio(List<String> videoUrls, String tema) throws IOException {

        String audioFileName = null;

        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "영상 URL이 없습니다.");
        }

        if (tema != null && !tema.isBlank()) {
            audioFileName = switch (tema.toLowerCase()) {
                case "forest" -> "forest.mp3";
                case "water" -> "water.mp3"; // 오타 수정
                case "wave"  -> "wave.mp3";
                case "fun"   -> "fun.mp3";
                case "sad"   -> "sad.mp3";
                default -> throw new BusinessException(ErrorCode.AUDIO_NOT_FOUND, "요청 정보를 찾을 수 없습니다.");
            };
        }

        String uuid = UUID.randomUUID().toString();
        File tempDir = new File("temp_" + uuid);
        if (!tempDir.exists()) tempDir.mkdirs();

        // 1. 영상 다운로드
        List<File> downloadedVideos = downloadVideos(videoUrls, tempDir);

        File concatListFile = new File(tempDir, "concat_list.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatListFile))) {
            for (File video : downloadedVideos) {
                writer.write("file '" + video.getAbsolutePath() + "'");
                writer.newLine();
            }
        }

        // 2. 영상 병합
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

        // 3. 영상 길이 측정
        FFmpegProbeResult videoInfo = ffprobe.probe(mergedVideo.getAbsolutePath());
        double duration = videoInfo.getFormat().duration;

        // 4. 최종 결과물 파일 경로 설정
        File finalOutput = new File("output_" + uuid + ".mp4");

        // 5. 오디오가 있는 경우에만 트리밍 + 병합
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

            FFmpegBuilder mergeBuilder = new FFmpegBuilder()
                    .addInput(mergedVideo.getAbsolutePath())
                    .addInput(trimmedAudio.getAbsolutePath())
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
            // 오디오가 없으면 영상 그대로 복사
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
