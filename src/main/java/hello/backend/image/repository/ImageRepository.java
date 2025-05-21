package hello.backend.image.repository;

import hello.backend.image.domain.Image;
import hello.backend.user.domain.User;
import hello.backend.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByUserId(Long user_id);
    Optional<Image> findByFinalImage(String finalImage);
    List<Image> findAllByUserNot(User user);
}
