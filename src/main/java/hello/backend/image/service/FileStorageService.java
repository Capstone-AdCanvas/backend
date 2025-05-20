package hello.backend.image.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
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
import java.util.*;

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

    // 최종 이미지 저장
    @Transactional
    public String chooseFinalImage(File localFile, String userId, Long imageId, String subDir) {
        try {
            byte[] imageBytes = Files.readAllBytes(localFile.toPath());
            String fileName = "final_" + imageId + ".png";
            String objectPath = "uploads/" + userId + "/"+ subDir + "/" + fileName;

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                    .setContentType("image/png")
                    .build();

            storage.create(blobInfo, imageBytes);
            storage.get(blobInfo.getBlobId()).toBuilder()
                    .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                    .build()
                    .update();

            return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.GCS_UPLOAD_FAILED);
        }
    }

    public void cleanUpDir(Long userId) {
        String prefix = "uploads/" + userId + "/";
        Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix));
        List<BlobId> blobIdsToDelete = new ArrayList<>();

        for (Blob blob : blobs.iterateAll()) {
            blobIdsToDelete.add(blob.getBlobId());
        }

        if (blobIdsToDelete.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        List<Boolean> deleteResults = storage.delete(blobIdsToDelete);

        for (int i = 0; i < deleteResults.size(); i++) {
            if (!Boolean.TRUE.equals(deleteResults.get(i))) {
                String failedFile = blobIdsToDelete.get(i).getName();
                throw new BusinessException(ErrorCode.GCS_FILE_DELETE_FAILED, "삭제 실패한 파일: " + failedFile);
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
