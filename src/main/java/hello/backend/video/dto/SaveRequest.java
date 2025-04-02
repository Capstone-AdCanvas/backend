package hello.backend.video.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class SaveRequest {
    private String name;
    private String videoUrl;
    private String aspectRatio;
    private String duration;
    private LocalDateTime createdAt;
}
