package hello.backend.image.service;

import hello.backend.exception.InvalidFileException;
import hello.backend.exception.NotFoundException;
import hello.backend.image.domain.AdImage;
import hello.backend.image.dto.ImageResponse;
import hello.backend.image.repository.AdImageRepository;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdImageService {

    private final AdImageRepository adImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // 이미지 업로드
    @Transactional
    public ImageResponse uploadImage(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if (image.isEmpty()) {
            throw new InvalidFileException("업로드된 파일이 비어 있습니다.");
        }

        String savedFilePath = fileStorageService.saveFile(image);

        AdImage adImage = AdImage.builder()
                .user(user)
                .originalImage(savedFilePath)
                .build();

        AdImage savedImage = adImageRepository.save(adImage);

        return toImageResponse(savedImage);
    }

    // 전체 이미지 조회
    public List<ImageResponse> getAllImages() {
        List<AdImage> images = adImageRepository.findAll();
        return images.stream()
                .map(this::toImageResponse)
                .toList();
    }

    // 사용자 이미지 조회
    public List<ImageResponse> getUserImages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        List<AdImage> images = adImageRepository
                .findAllByUserId(user.getId());

        return images.stream()
                .map(this::toImageResponse)
                .toList();
    }

    // 특정 이미지 조회
    public ImageResponse getImage(Long imageId) {
        AdImage image = adImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다."));
        return toImageResponse(image);
    }

    private ImageResponse toImageResponse(AdImage adImage) {
        return new ImageResponse(
                adImage.getId(),
                adImage.getUser().getId(),
                adImage.getOriginalImage()
        );
    }

}
