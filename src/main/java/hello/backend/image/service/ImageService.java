package hello.backend.image.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.image.domain.Image;
import hello.backend.image.dto.CombineImageRequest;
import hello.backend.image.dto.CombineImageResponse;
import hello.backend.image.dto.ImageResponse;
import hello.backend.image.dto.OverlayItemRequest;
import hello.backend.image.repository.ImageRepository;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ImageFileService ImageFileService;

    // 이미지 업로드 (기본)
    public ImageResponse uploadImage(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String savedFilePath = ImageFileService.saveFile(image, userId.toString(), "original");

        Image adImage = Image.builder()
                .user(user)
                .originalImage(savedFilePath)
                .build();

        Image savedImage = imageRepository.save(adImage);

        return toOriginalImageResponse(savedImage);
    }

    // 전체 이미지 조회
    public List<ImageResponse> getAllImagesExceptCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Image> images = imageRepository.findAllByUserNot(user);
        return images.stream()
                .map(this::toFinalImageResponse)
                .toList();
    }

    // 사용자 이미지 조회
    public List<ImageResponse> getUserImages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Image> images = imageRepository
                .findAllByUserId(user.getId());

        return images.stream()
                .map(this::toFinalImageResponse)
                .toList();
    }

    // 특정 이미지 조회
    public ImageResponse getImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
        return toFinalImageResponse(image);
    }

    // 이미지 합성
    public CombineImageResponse combineImage(CombineImageRequest request) {
        Image image = imageRepository.findByFinalImage(request.getBaseImage())
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        try {
            BufferedImage baseImage = ImageIO.read(new URL(image.getFinalImage()));
            Graphics2D g2d = baseImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (OverlayItemRequest overlay : request.getOverlays()) {
                if ("logo".equalsIgnoreCase(overlay.getType())) {
                    BufferedImage logoImage = ImageIO.read(new URL(overlay.getImageUrl()));
                    int logoWidth = (int) (logoImage.getWidth() * overlay.getScale());
                    int logoHeight = (int) (logoImage.getHeight() * overlay.getScale());
                    java.awt.Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, java.awt.Image.SCALE_SMOOTH);
                    g2d.drawImage(scaledLogo, overlay.getX(), overlay.getY(), logoWidth, logoHeight, null);
                } else if ("text".equalsIgnoreCase(overlay.getType())) {
                    g2d.setFont(new Font(overlay.getFont(), Font.PLAIN, overlay.getSize()));
                    g2d.setColor(Color.decode(overlay.getColor()));
                    g2d.drawString(overlay.getText(), overlay.getX(), overlay.getY());
                }
            }

            g2d.dispose();

            String finalImageUrl = ImageFileService.uploadCombinedImageToFinal(baseImage, image.getUser().getId(), image.getId());
            image.setFinalImage(finalImageUrl);
            imageRepository.save(image);

            return new CombineImageResponse(image.getId(), finalImageUrl);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.IMAGE_COMBINE_FAILED);
        }
    }

    private ImageResponse toOriginalImageResponse(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getUser().getId(),
                image.getName(),
                image.getOriginalImage()
        );
    }

    private ImageResponse toFinalImageResponse(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getUser().getId(),
                image.getName(),
                image.getFinalImage()
        );
    }
}
