package hello.backend.image.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.temp-dir}")
    private String tempDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    // 이미지 저장 (업로드)
    @Transactional
    public String saveFile(MultipartFile image) {
        try {
            ensureDirectoryExists(uploadDir);

            String extension = getFileExtension(image.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + "." + extension;
            String filePath = uploadDir + File.separator + newFileName;

            File dest = new File(filePath);
            image.transferTo(dest);

            if (!Files.exists(dest.toPath())) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
            }

            return filePath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    // 이미지 저장 (배경 제거, 임시)
    @Transactional
    public void saveBgRemoveFile(byte[] imageBytes, String filePath) {
        try {
            ensureDirectoryExists(Paths.get(filePath).getParent().toString());
            Files.write(Paths.get(filePath), imageBytes);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    // 파일 이동 (복사 후 원본 삭제)
    @Transactional
    public void moveFile(File source, File target) {
        if (!source.exists()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        try {
            ensureDirectoryExists(target.getParentFile().getAbsolutePath());
            Files.copy(source.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            boolean deleted = source.delete();
            if (!deleted) {
                log.warn("원본 파일 삭제 실패: {}", source.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    @Transactional
    public String saveBase64Image(String base64Data) {
        try {
            ensureDirectoryExists(tempDir);

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String filePath = Paths.get(tempDir, "processed_" + UUID.randomUUID() + ".png").toString();

            Files.write(Paths.get(filePath), imageBytes);
            return filePath;
        } catch (IOException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    public void cleanUpTempDir() {
        File tempDirectory = new File(tempDir);
        File[] files = tempDirectory.listFiles();

        if (files == null) {
            log.warn("tempDir 경로가 잘못되었거나 접근 불가: {}", tempDir);
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("임시 파일 삭제 완료: {}", file.getAbsolutePath());
                } else {
                    log.warn("임시 파일 삭제 실패: {}", file.getAbsolutePath());
                }
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        return extension;
    }
}
