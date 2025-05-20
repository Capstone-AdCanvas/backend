package hello.backend.image.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Value("${file.temp-dir}")
    private String tempDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    // 이미지 업로드
    @Transactional
    public String saveFile(MultipartFile image, String userId, String subDir) {
        try {
            String extension = getFileExtension(image.getOriginalFilename());
            String newFileName = UUID.randomUUID() + "." + extension;
            String objectPath = "uploads/" + userId + "/" + subDir + "/" + newFileName;

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                    .setContentType(image.getContentType())
                    .build();

            storage.create(blobInfo, image.getBytes());

            storage.get(blobInfo.getBlobId()).toBuilder()
                    .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                    .build()
                    .update();

            return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
        }
    }

    // 이미지 저장 (배경 제거)
    @Transactional
    public String saveBgRemoveFile(byte[] imageBytes, String userId, Long imageId, String subDir) {
        String fileName = "processed_" + imageId + ".png";
        String objectPath = "uploads/" + userId + "/" + subDir + "/" + fileName;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                .setContentType("image/png")
                .build();

        storage.create(blobInfo, imageBytes);

        storage.get(blobInfo.getBlobId()).toBuilder()
                .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                .build()
                .update();

        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
    }

    // 배경 생성 이미지 저장
    @Transactional
    public String saveBase64Image(String base64Data, String userId, String subDir) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String fileName = "processed_" + UUID.randomUUID() + ".png";
            String objectPath = "uploads/" + userId + "/" + subDir + "/" + fileName;

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                    .setContentType("image/png")
                    .build();

            storage.create(blobInfo, imageBytes);

            storage.get(blobInfo.getBlobId()).toBuilder()
                    .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                    .build()
                    .update();

            return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
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

        } catch (IOException e) {
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
