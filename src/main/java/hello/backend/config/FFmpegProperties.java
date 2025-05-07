package hello.backend.config;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "ffmpeg")
public class FFmpegConfig {

    private String mpeg;
    private String probe;
    private String savePath;

}
