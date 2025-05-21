package hello.backend.video.repository;

import hello.backend.user.domain.User;
import hello.backend.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    boolean existsByName(String name);
    List<Video> findAllByUserOrderByCreatedAtDesc(User user);
    Optional<Video> findTopByIdOrderByCreatedAtDesc(Long videoId);
}
