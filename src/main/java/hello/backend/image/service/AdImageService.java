package hello.backend.image.service;

import hello.backend.exception.BadRequestException;
import hello.backend.image.domain.AdImage;
import hello.backend.image.repository.AdImageRepository;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdImageService {

    private final AdImageRepository adImageRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 이미지 업로드
    public AdImage uploadImage(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));

        if (image.isEmpty()) {
            throw new BadRequestException("업로드된 파일이 비어 있습니다.");
        }

        String savedFilePath = saveFile(image);

        AdImage adImage = AdImage.builder()
                .user(user)
                .originalImage(savedFilePath)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return adImageRepository.save(adImage);
    }

    // 이미지 저장
    public String saveFile(MultipartFile image) throws IOException {
        File directory = new File(uploadDir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new BadRequestException("파일 저장 경로를 생성할 수 없습니다.");
        }

        // 원본 파일명 확인
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BadRequestException("파일명이 유효하지 않습니다.");
        }

        // 확장자 확인
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BadRequestException("파일에 확장자가 없습니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;

        String filePath = uploadDir + newFileName;
        File dest = new File(filePath);
        image.transferTo(dest);

        // 저장 후 실제 존재하는지 확인
        if (!Files.exists(dest.toPath())) {
            throw new BadRequestException("파일 저장에 실패하였습니다.");
        }

        return filePath;
    }
}
