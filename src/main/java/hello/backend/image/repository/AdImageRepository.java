package hello.backend.image.repository;

import hello.backend.image.domain.AdImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AdImageRepository extends JpaRepository<AdImage, Long> {
    List<AdImage> findAllByUserId(Long user_id);
}
