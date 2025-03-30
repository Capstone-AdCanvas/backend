package hello.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${DRAPH_ART_URL}")
    private String draphArtUrl;

    @Value("${DRAPH_ART_TOKEN}")
    private String API_TOKEN;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(draphArtUrl)
                .defaultHeader("Authorization", "Bearer " + API_TOKEN)
                .build();
    }
}
