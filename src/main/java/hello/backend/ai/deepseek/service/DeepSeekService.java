package hello.backend.ai.deepseek.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final RestTemplate restTemplate;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    // text prompt
    public String textTransFormScript(String userScript) {
        String prompt = """
            Convert the following sentence into a short, natural scenario suitable for an advertising video scene.
                - The main theme is "<Insert your main keyword here>".
                - The product must remain stationary and must not move.
                - The product must always remain sharply in focus, clearly visible, and be the primary visual element of the scene.
                - Natural elements should remain complementary and subtle, enhancing but never distracting from the clearly-focused product.
                - Keep it concise and poetic, within approximately 3 sentences, and realistic enough to film directly.
                - Provide your response entirely in English.
            The sentence I want you to convert is:
            """ + userScript;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
        return message.get("content");
    }

    // image prompt
    public String imageTransFormScript(String userScript) {
        String prompt = """
            Convert the following sentence into a short, natural scenario suitable for an advertising video scene.
                - The main theme is "<Insert your main keyword here>".
                - The product must remain stationary and must not move.
                - The product must always remain sharply in focus, clearly visible, and be the primary visual element of the scene. Never blur, obscure, or overshadow the product with any natural elements, transitions, or camera effects.
                - Natural elements should remain complementary and subtle, enhancing but never distracting from the clearly-focused product.
                - Keep it concise and poetic, within approximately 3 sentences, and realistic enough to film directly.
                - Provide your response entirely in English.
            The sentence I want you to convert is:
            """ + userScript;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
        return message.get("content");
    }
}