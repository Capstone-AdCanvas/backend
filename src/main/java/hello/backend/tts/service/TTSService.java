package hello.backend.tts.service;

import hello.backend.ai.deepseek.service.DeepSeekService;
import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.tts.dto.TTSRequest;
import hello.backend.tts.dto.TTSResponse;
import hello.backend.tts.dto.enums.TTSModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TTSService {

    private final DeepSeekService deepSeekService;
    private final WebClient clovaTtsWebClient;

    public TTSService(@Qualifier("clovaTtsWebClient") WebClient clovaTtsWebClient,
                      DeepSeekService deepSeekService) {
        this.clovaTtsWebClient = clovaTtsWebClient;
        this.deepSeekService = deepSeekService;
    }

    // tts 모델 조회
    public List<TTSResponse> getTTSModel() {
        return Arrays.stream(TTSModel.values())
                .map(TTSResponse::from)
                .collect(Collectors.toList());
    }

    public Mono<List<byte[]>> getTtsAudioListAsync(TTSRequest ttsRequest) {

        int chunkCount = ttsRequest.getSecond() / 5;
        List<String> chunks = deepSeekService.generateMultipleImageScripts(ttsRequest.getText(), chunkCount);
        List<Mono<byte[]>> audioRequests = chunks.stream()
                .map(chunkText -> sendTtsRequest(chunkText, ttsRequest))
                .collect(Collectors.toList());

        return Flux.merge(audioRequests).collectList();
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

    public byte[] zipAudioFiles(List<byte[]> audioList) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            for (int i = 0; i < audioList.size(); i++) {
                zipOut.putNextEntry(new ZipEntry("speech_" + i + ".mp3"));
                zipOut.write(audioList.get(i));
                zipOut.closeEntry();
            }
            zipOut.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("ZIP 생성 실패", e);
        }
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
