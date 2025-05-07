package hello.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Value("${DRAPH_ART_URL}")
    private String draphArtUrl;

    @Value("${DRAPH_ART_TOKEN}")
    private String API_TOKEN;

    @Value("${CLOVA_TTS_URL}")
    private String clovaTtsUrl;

    @Value("${CLOVA_TTS_CLIENT_ID}")
    private String clovaClientId;

    @Value("${CLOVA_TTS_CLIENT_SECRET}")
    private String clovaClientSecret;

    @Bean(name = "draphArtWebClient")
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(draphArtUrl)
                .defaultHeader("Authorization", "Bearer " + API_TOKEN)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean(name = "clovaTtsWebClient")
    public WebClient clovaTtsWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 20MB
                .build();

        return WebClient.builder()
                .baseUrl(clovaTtsUrl)
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clovaClientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clovaClientSecret)
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .exchangeStrategies(strategies)
                .build();
    }
}
