package hello.backend.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
public class SaveResponse {
    private Long userId;
    private String name;
    private String videoUrl;
    private String aspectRatio;
    private String duration;
    private LocalDateTime createdAt;
}
