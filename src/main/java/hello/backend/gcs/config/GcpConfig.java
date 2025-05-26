package hello.backend.gcs.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Configuration
public class GcpConfig {

    @Value("${gcp.credentials}")
    private String credentialsPath;

    @Bean
    public Storage storage() throws IOException {
        File file = new File(credentialsPath);

        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build()
                    .getService();
        }
    }
}
