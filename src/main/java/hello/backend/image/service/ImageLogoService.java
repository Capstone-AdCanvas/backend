package hello.backend.image.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.image.domain.Logo;
import hello.backend.image.dto.LogoResponse;
import hello.backend.image.repository.LogoRepository;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageLogoService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final LogoRepository logoRepository;

    // 이미지 업로드
    @Transactional
    public LogoResponse uploadImage(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String savedFilePath = fileStorageService.saveLogoFile(image);

        Logo adLogo = Logo.builder()
                .user(user)
                .logoImage(savedFilePath)
                .build();

        Logo savedImage = logoRepository.save(adLogo);

        return toLogoResponse(savedImage);
    }

    // 사용자 업로드 로고 조회
    @Transactional
    public List<LogoResponse> getLogoImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<Logo> logos = logoRepository
                .findAllByUserId(user.getId());

        return logos.stream()
                .map(this::toLogoResponse)
                .toList();

    }

    private LogoResponse toLogoResponse(Logo logo) {
        return new LogoResponse(
                logo.getId(),
                logo.getUser().getId(),
                logo.getLogoImage()
        );
    }
}
