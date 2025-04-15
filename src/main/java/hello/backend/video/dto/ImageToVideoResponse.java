package hello.backend.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ImageToVideoResponse {
    private String status;
    private String videoUrl;
    private String duration;
    private String aspect_ratio;
    private LocalDateTime createdAt;
}
