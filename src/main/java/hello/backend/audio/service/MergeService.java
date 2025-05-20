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
public class AudioService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    @Transactional
    public File mergeVideoAudio(List<String> videoUrls, String tema) throws IOException {

        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "영상 URL이 없습니다.");
        }

        String audioFileName = switch (tema.toLowerCase()) {
            case "forest" -> "forest.mp3";
            case "water" -> "fater.mp3";
            case "wave" -> "wave.mp3";
            case "fun" -> "fun.mp3";
            case "sad" -> "sad.mp3";
            default -> throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "요청 정보를 찾을 수 없습니다.");
        };

        String uuid = UUID.randomUUID().toString();
        File tempDir = new File("temp_" + uuid);
        if (!tempDir.exists())  tempDir.mkdirs();

        List<File> downloadedVideos = downloadVideos(videoUrls, tempDir);

        File concatListFile = new File(tempDir, "concat_list.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatListFile))) {
            for (File video : downloadedVideos) {
                writer.write("file '" + video.getAbsolutePath() + "'");
                writer.newLine();
            }
        }

        File mergedVideo = new File(tempDir, "merged.mp4");
        FFmpegBuilder concatBuilder = new FFmpegBuilder()
                .setInput(concatListFile.getAbsolutePath())
                .addExtraArgs("-f", "concat", "-safe", "0")
                .overrideOutputFiles(true)
                .addOutput(mergedVideo.getAbsolutePath())
                .setFormat("mp4")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(concatBuilder).run();

        FFmpegProbeResult videoInfo = ffprobe.probe(mergedVideo.getAbsolutePath());
        double duration = videoInfo.getFormat().duration;

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

        // 최종 영상 + 오디오 합성
        File finalOutput = new File("output_" + uuid + ".mp4");
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
