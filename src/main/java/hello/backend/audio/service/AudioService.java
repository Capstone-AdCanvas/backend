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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    @Transactional
    public File mergeVideoAudio(String videoUrl, String tema) throws IOException {

        String audioFileName = switch (tema.toLowerCase()) {
            case "forest" -> "Forest.mp3";
            case "water" -> "Water.mp3";
            case "wave" -> "Wave.mp3";
            default -> throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "요청 정보를 찾을 수 없습니다.");
        };

        String audioPath = new File("src/main/resources/static/mp3/" + audioFileName).getAbsolutePath();
        String outputPath = "output_" + UUID.randomUUID() + ".mp4";
        FFmpegProbeResult videoInfo = ffprobe.probe(videoUrl);
        double duration = videoInfo.getFormat().duration;

        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput(videoUrl)
                .addInput(audioPath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setVideoCodec("copy")
                .setAudioCodec("aac")
                .setFormat("mp4")
                .addExtraArgs("-map", "0:v:0")
                .addExtraArgs("-map", "1:a:0")
                .addExtraArgs("-t", String.valueOf(duration))
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        return new File(outputPath);
    }
}
