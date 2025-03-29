package hello.backend.image.service;

import hello.backend.exception.BadRequestException;
import hello.backend.exception.InvalidFileException;
import hello.backend.exception.UnsupportedMediaTypeException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    // 이미지 저장
    @Transactional
    public String saveFile(MultipartFile image) {
        try {
            File directory = new File(uploadDir);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new BadRequestException("파일 저장 경로를 생성할 수 없습니다.");
            }

            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new InvalidFileException("파일명이 유효하지 않습니다.");
            }

            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex == -1) {
                throw new InvalidFileException("파일 확장자가 없습니다.");
            }

            String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new UnsupportedMediaTypeException("지원되지 않는 파일 형식입니다. (허용된 확장자: png, jpg, jpeg)");
            }

            String newFileName = UUID.randomUUID().toString() + "." + extension;
            String filePath = uploadDir + File.separator + newFileName;

            File dest = new File(filePath);
            image.transferTo(dest);

            if (!Files.exists(dest.toPath())) {
                throw new BadRequestException("파일 저장에 실패하였습니다.");
            }

            return filePath;
        } catch (IOException e) {
            throw new InvalidFileException("파일 저장 중 오류가 발생했습니다.");
        }
    }

    // 배경제거 이미지 파일로 저장
    @Transactional
    public void saveBgRemoveFile(byte[] imageBytes, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(Paths.get(filePath), imageBytes);
        } catch (IOException e) {
            throw new InvalidFileException("파일 저장 중 오류가 발생했습니다.");
        }
    }
}
