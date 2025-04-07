package hello.backend.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SaveResponse {
    private Long userId;
    private String name;
    private String videoUrl;
    private String aspectRatio;
    private String duration;
    private LocalDateTime createdAt;
}
