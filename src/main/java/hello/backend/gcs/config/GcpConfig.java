package hello.backend.gcs.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcpConfig {

    @Bean
    public Storage storage() throws IOException {
        // resources/gcs-service-key.json 을 classpath에서 불러옴
        ClassPathResource resource = new ClassPathResource("project-adcanvas-7198154f5844.json");

        try (InputStream inputStream = resource.getInputStream()) {
            return StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build()
                    .getService();
        }
    }
}
