package hello.backend.image.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.image.domain.Image;
import hello.backend.image.dto.ImageResponse;
import hello.backend.image.repository.ImageRepository;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // 이미지 업로드
    @Transactional
    public ImageResponse uploadImage(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String savedFilePath = fileStorageService.saveFile(image);

        Image adImage = Image.builder()
                .user(user)
                .originalImage(savedFilePath)
                .build();

        Image savedImage = imageRepository.save(adImage);

        return toImageResponse(savedImage);
    }

    // 전체 이미지 조회
    public List<ImageResponse> getAllImages() {
        List<Image> images = imageRepository.findAll();
        return images.stream()
                .map(this::toImageResponse)
                .toList();
    }

    // 사용자 이미지 조회
    public List<ImageResponse> getUserImages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Image> images = imageRepository
                .findAllByUserId(user.getId());

        return images.stream()
                .map(this::toImageResponse)
                .toList();
    }

    // 특정 이미지 조회
    public ImageResponse getImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
        return toImageResponse(image);
    }

    private ImageResponse toImageResponse(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getUser().getId(),
                image.getOriginalImage()
        );
    }

}
