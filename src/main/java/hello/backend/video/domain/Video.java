package hello.backend.video.domain;

import hello.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ad_video")
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String finalVideo;

    @Column()
    private String aspectRatio;

    @Column(nullable = false)
    private String duration;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;


    @Builder
    public Video(Long id, String name, User user, String finalVideo, String aspectRatio, String duration, LocalDateTime createAt) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.finalVideo = finalVideo;
        this.aspectRatio = aspectRatio;
        this.duration = duration;
        this.createAt = createAt;
    }
}
