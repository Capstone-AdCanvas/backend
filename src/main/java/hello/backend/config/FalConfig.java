package hello.backend.config;

import ai.fal.client.FalClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FalConfig {

    @Value("${fal.token}")
    private String falToken;

    @Bean
    public FalClient falClient() {
        System.setProperty("FAL_KEY", falToken); // 명시적 설정
        return FalClient.withEnvCredentials();
    }
}
