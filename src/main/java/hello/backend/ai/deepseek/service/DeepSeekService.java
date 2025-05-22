package hello.backend.ai.deepseek.service;

import hello.backend.video.dto.TextToVideoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
            You are a public awareness video creator. Based on the sentence below, create a 10-second video scenario. \s
            The video must consist of **2 distinct scenes**, each lasting about **5 seconds**, flowing naturally from one to the next. \s
            Each scene should be realistic, filmable, and focused on visually clear transitions — no text, narration, or subtitles. \s
            Avoid poetic or abstract descriptions. Focus on grounded, everyday visuals that can be captured in a real-world setting. \s
            Describe each scene clearly in **English**, with simple, specific details. \s
            The user-submitted sentence is:
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

    // tts 전달 프롬프트 생성
    public List<String> generateMultipleImageScripts(String userScript, int chunkCount) {
        List<String> scripts = new ArrayList<>();

        String[] sentenceChunks = userScript.split("[.!?\\n]\\s*");

        List<String> validSentences = Arrays.stream(sentenceChunks)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<String> adjustedChunks = new ArrayList<>();
        int totalLength = validSentences.stream().mapToInt(String::length).sum();
        int avgLengthPerChunk = Math.max(totalLength / chunkCount, 1);

        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : validSentences) {
            if (currentChunk.length() + sentence.length() < avgLengthPerChunk || adjustedChunks.size() + 1 == chunkCount) {
                currentChunk.append(sentence).append(" ");
            } else {
                adjustedChunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder(sentence).append(" ");
            }
        }
        if (!currentChunk.isEmpty()) {
            adjustedChunks.add(currentChunk.toString().trim());
        }

        while (adjustedChunks.size() < chunkCount) {
            adjustedChunks.add(adjustedChunks.get(adjustedChunks.size() - 1));
        }

        for (int i = 0; i < chunkCount; i++) {
            String script = adjustedChunks.get(i);
            String prompt = """
            Please write exactly one unique and creative voiceover script for an advertising video based on the sentence below.
            - Theme: advertising (including public service announcements).
            - The script must reflect a *different perspective or message* from others.
            - Do not repeat similar phrases or sentence structures from previous outputs.
            - Use emotional but authentic tone. Avoid exaggeration.
            - Length: 1 sentences, about 3 seconds.
            - It must be exactly 12~14 Korean syllables (characters).
            - Output only the final Korean script. No lists, no numbers, no explanations.
            Sentence for inspiration:
            """ + script;

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

            String content = message.get("content").split("\\n")[0].replaceAll("^\\d+\\.?\\s*", "").trim();
            scripts.add(content);

            log.info("Script " + (i + 1) + ": " + content);
        }

        return scripts;
    }

    //병렬 프롬프트 리라이팅
    //-----------------------------------------------------------
    public List<String> textPartitioningTransFormScript(TextToVideoRequest request) {

        int count = request.getSecond() / 5;

        log.info("second: {}", request.getSecond());
        log.info("count: {}", count);

        String prompt = """
            You are a public awareness video creator.
                                                   Based on the sentence below, create a {second} second video scenario.
                                                   The video **must** consist of **exactly {count} distinct scenes**. Each scene should be exactly **5 seconds** long, flowing naturally from one to the next.
                                                   The sequence of these {count} scenes should form a coherent narrative with a clear progression (e.g., introduction, development, climax, resolution), appropriately adapted for the {count} scenes.
                                                   Each scene should be realistic, filmable, and focused on visually clear transitions — no text, narration, or subtitles.
                                                   Avoid poetic or abstract descriptions. Focus on grounded, everyday visuals that can be captured in a real-world setting.
                                                   Describe each scene clearly in **English**, with simple, specific details. **Ensure each scene description ends with an ellipsis (...).**
                
                                                   **Important Formatting Instructions:**
                                                   Please provide the description for each of the {count} scenes.
                                                   Ensure that each scene's description is separated from the next **using a double newline character only**. Do not include scene numbers like "Scene 1:", "Scene 2:" unless it's naturally part of the description.
                
                                                   For example, if creating 3 scenes, the output should look like this:
                                                   [Detailed, filmable description for the first 5-second scene, focusing on visuals...]
                
                                                   [Detailed, filmable description for the second 5-second scene, flowing from the first, focusing on visuals...]
                
                                                   [Detailed, filmable description for the third 5-second scene, flowing from the second, focusing on visuals...]
                
                                                   The user-submitted sentence is:
        """ + request.getPrompt();

        String finalPrompt = prompt
                .replace("{second}", String.valueOf(request.getSecond()))
                .replace("{count}", String.valueOf(count));

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", finalPrompt
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, String> message = (Map<String, String>) firstChoice.get("message");

        List<String> stepList = new ArrayList<>();

        String[] stepArray = message.get("content").split("\n\n");

        for (int i = 0; i < stepArray.length; i++) {
            stepList.add(stepArray[i]);
        }

        return stepList;
    }
}