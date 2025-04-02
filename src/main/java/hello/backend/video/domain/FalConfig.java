package hello.backend.video.domain;

import ai.fal.client.FalClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FalConfig {
    @Bean
    public FalClient falClient() {
        return  FalClient.withEnvCredentials();
    }
}
