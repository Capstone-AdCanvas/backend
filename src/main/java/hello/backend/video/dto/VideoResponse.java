package hello.backend.video.dto;

import hello.backend.user.domain.User;
import hello.backend.video.domain.Video;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private Long id;

    private String name;

    private Long userId;

    private String finalVideo;

    private String aspectRatio;

    private String duration;

    private LocalDateTime createdAt;

    public VideoResponse(Video video) {
        this.id = video.getId();
        this.name = video.getName();
        this.userId = video.getUser().getId();
        this.finalVideo = video.getFinalVideo();
        this.aspectRatio = video.getAspectRatio();
        this.duration = video.getDuration();
        this.createdAt = video.getCreatedAt();
    }
}
