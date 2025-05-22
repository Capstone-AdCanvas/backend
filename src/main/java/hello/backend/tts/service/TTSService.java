package hello.backend.tts.service;

import hello.backend.ai.deepseek.service.DeepSeekService;
import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.tts.dto.TTSModelResponse;
import hello.backend.tts.dto.TTSRequest;
import hello.backend.tts.dto.TTSResponse;
import hello.backend.tts.dto.enums.TTSModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TTSService {

    @Value("${file.tts-dir}")
    private String ttsDir;

    private final DeepSeekService deepSeekService;
    private final WebClient clovaTtsWebClient;

    public TTSService(@Qualifier("clovaTtsWebClient") WebClient clovaTtsWebClient,
                      DeepSeekService deepSeekService) {
        this.clovaTtsWebClient = clovaTtsWebClient;
        this.deepSeekService = deepSeekService;
    }

    // tts 모델 조회
    public List<TTSModelResponse> getTTSModel() {
        return Arrays.stream(TTSModel.values())
                .map(TTSModelResponse::from)
                .collect(Collectors.toList());
    }

    public Mono<List<String>> getTtsAudioListAsync(TTSRequest request) {
        int chunkCount = Math.max(1, request.getSecond() / 5);

        List<String> ttsTexts = deepSeekService.generateMultipleImageScripts(request.getText(), chunkCount);
        List<Mono<String>> fileMonos = new ArrayList<>();

        for (int i = 0; i < ttsTexts.size(); i++) {
            String sentence = ttsTexts.get(i);
            int index = i;

            Mono<String> fileMono = sendTtsRequest(sentence, request)
                    .flatMap(audioBytes -> {
                        String filename = "audio_" + System.currentTimeMillis() + "_" + index + ".mp3";
                        Path filePath = Paths.get(ttsDir, filename);
                        try {
                            Files.createDirectories(filePath.getParent());
                            Files.write(filePath, audioBytes);
                            return Mono.just(filename);
                        } catch (IOException e) {
                            return Mono.error(new BusinessException(ErrorCode.TTS_FILE_SAVE_FAILED));
                        }
                    });

            fileMonos.add(fileMono);
        }

        return Flux.merge(fileMonos).collectList();
    }

    private Mono<byte[]> sendTtsRequest(String chunkText, TTSRequest originalRequest) {
        return clovaTtsWebClient.post()
                .uri("/tts")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("speaker", originalRequest.getSpeaker())
                        .with("text", chunkText)
                        .with("emotion", String.valueOf(originalRequest.getEmotion()))
                        .with("emotion-strength", String.valueOf(originalRequest.getEmotionStrength()))
                        .with("speed", String.valueOf(-1)))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .map(TTSService::extractClovaErrorCode)
                                .map(BusinessException::new)
                                .flatMap(Mono::error)
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new BusinessException(ErrorCode.CLOVA_TTS_INTERNAL_ERROR))
                )
                .bodyToMono(byte[].class);
    }

    private static ErrorCode extractClovaErrorCode(String body) {
        if (body.contains("VS01")) return ErrorCode.CLOVA_TTS_SPEAKER_REQUIRED;
        if (body.contains("VS02")) return ErrorCode.CLOVA_TTS_UNSUPPORTED_SPEAKER;
        if (body.contains("VS03")) return ErrorCode.CLOVA_TTS_SPEED_REQUIRED;
        if (body.contains("VS04")) return ErrorCode.CLOVA_TTS_UNSUPPORTED_SPEED;
        if (body.contains("VS05")) return ErrorCode.CLOVA_TTS_TEXT_REQUIRED;
        if (body.contains("VS06")) return ErrorCode.CLOVA_TTS_TEXT_TOO_LONG;
        if (body.contains("VS14")) return ErrorCode.CLOVA_TTS_UNSUPPORTED_EMOTION;
        if (body.contains("VS18")) return ErrorCode.CLOVA_TTS_SENTENCE_TOO_LONG;
        if (body.contains("VS19")) return ErrorCode.CLOVA_TTS_UNSUPPORTED_EMOTION_STRENGTH;
        if (body.contains("VS26")) return ErrorCode.CLOVA_TTS_FAILED;
        return ErrorCode.CLOVA_TTS_INTERNAL_ERROR;
    }
}
