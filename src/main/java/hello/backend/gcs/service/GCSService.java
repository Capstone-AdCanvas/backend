package hello.backend.gcs.service;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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
}