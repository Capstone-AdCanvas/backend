package hello.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${file.tts-dir}")
    private String ttsDir;

    @Value("${file.tts-url}")
    private String ttsUrl;

    @Value("${file.video-dir}")
    private String videoDir;

    @Value("${file.video-url}")
    private String videoUrl;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler(ttsUrl + "**")
                        .addResourceLocations("file:" + ttsDir + "/");

                registry.addResourceHandler(videoUrl + "**")
                        .addResourceLocations("file:" + videoDir + "/");

                registry.addResourceHandler("/images/**")
                        .addResourceLocations("https://storage.googleapis.com/" + bucketName + "/")
                        .setCachePeriod(3600)
                        .resourceChain(true);
            }
        };
    }
}
