package hello.backend.image.repository;

import hello.backend.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByUserId(Long user_id);
    Optional<Image> findByFinalImage(String finalImage);
    Image findFirstById(Long id);
}
