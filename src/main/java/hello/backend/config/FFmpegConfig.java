package hello.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FFmpegConfig {

    private final FFmpegProperties fFmpegProperties;
    private static String SAVE_PATH;

    @PostConstruct
    public void init() {
        SAVE_PATH = fFmpegProperties.getSavePath();
        log.info("[FFmpegConfig] Save path initialized to: " + SAVE_PATH);
    }

    @Bean
    public FFmpeg ffmpeg() throws IOException {
        return new FFmpeg(fFmpegProperties.getMpeg());
    }

    @Bean
    public FFprobe ffprobe() throws IOException {
        return new FFprobe(fFmpegProperties.getProbe());
    }

    @Bean
    public FFmpegExecutor ffmpegExecutor(FFmpeg ffmpeg, FFprobe ffprobe) {
        return new FFmpegExecutor(ffmpeg, ffprobe);
    }
}
