package hello.backend.gcs.service;


import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GCSService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public File downloadVideoFromUrl(String url, String filename) throws IOException {
        Path tempFile = Files.createTempFile("video_", "_" + filename);
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toFile();
    }

    public String upload(File file, String userId, String subDir) throws IOException {
        String objectName = userId + "/" + subDir + "/" + file.getName();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(Files.probeContentType(file.toPath()))
                .build();

        storage.create(blobInfo, Files.readAllBytes(file.toPath()));

        return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
    }

    // 이미지 저장
    public String uploadToGCS(byte[] imageBytes, String userId, String subDir, String fileName, String contentType) {
        String objectPath = "uploads/" + userId + "/" + subDir + "/" + fileName;
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath)
                .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, imageBytes);

        return "https://storage.googleapis.com/" + bucketName + "/" + objectPath;
    }

    // 디렉토리 비우기
    public void cleanUpDir(Long userId) {
        String prefix = "uploads/" + userId + "/";
        Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix));
        List<BlobId> blobIdsToDelete = new ArrayList<>();

        for (Blob blob : blobs.iterateAll()) {
            String blobName = blob.getName();
            if (blobName.startsWith("uploads/" + userId + "/final/") || blobName.startsWith("uploads/" + userId + "/logo/")) {
                continue;
            }
            blobIdsToDelete.add(blob.getBlobId());
        }

        if (blobIdsToDelete.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        List<Boolean> deleteResults = storage.delete(blobIdsToDelete);

        for (int i = 0; i < deleteResults.size(); i++) {
            if (!Boolean.TRUE.equals(deleteResults.get(i))) {
                throw new BusinessException(ErrorCode.GCS_FILE_DELETE_FAILED);
            }
        }
    }
}