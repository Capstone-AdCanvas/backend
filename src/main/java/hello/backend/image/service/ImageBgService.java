package hello.backend.image.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.image.domain.Image;
import hello.backend.image.domain.enums.ImageTheme;
import hello.backend.image.dto.*;
import hello.backend.image.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageBgService {

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    @Qualifier("draphArtWebClient")
    private final WebClient draphArtWebClient;

    @Value("${DRAPH_ART_USERNAME}")
    private String USERNAME;

    @Value("${file.bgremove-dir}")
    private String bgRemoveDir;

    @Value("${file.temp-dir}")
    private String tempDir;

    @Value("${file.final-dir}")
    private String finalDir;

    @Value("${file.final-url}")
    private String finalUrl;

    // 이미지 배경 제거
    @Transactional
    public BgRemoveResponse removeBg(Long imageId) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        Path tempFile = Files.createTempFile("bg_input_", ".png");
        try (InputStream in = new URL(image.getOriginalImage()).openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        FileSystemResource fileResource = new FileSystemResource(tempFile.toFile());

        Map<String, Object> conceptOptionMap = new HashMap<>();
        conceptOptionMap.put("product_size", "auto");
        String conceptOptionJson = new ObjectMapper().writeValueAsString(conceptOptionMap);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("username", USERNAME);
        formData.add("gen_type", "remove_bg");
        formData.add("image", fileResource);
        formData.add("concept_option", conceptOptionJson);

        String base64ArrayString = draphArtWebClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> base64List = objectMapper.readValue(base64ArrayString, new TypeReference<List<String>>() {});

            if (base64List.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
            }

            String base64 = base64List.get(0);
            byte[] imageBytes = Base64.getDecoder().decode(base64);

            String processedImageUrl = fileStorageService.saveBgRemoveFile(imageBytes, image.getUser().getId().toString(), image.getId(), "bgremove");
            image.setProcessedImage(processedImageUrl);
            imageRepository.save(image);

            return toBgRemoveResponse(image);
        } catch (Exception e) {
            log.error("배경 제거 처리 중 예외 발생", e);
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    // 배경 생성
    @Transactional
    public List<BgGenerateResponse> generateBg(Long imageId, BgGenerateRequest request) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        Path tempFile = Files.createTempFile("bg_processed_", ".png");
        try (InputStream in = new URL(image.getProcessedImage()).openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        ImageTheme theme = Arrays.stream(ImageTheme.values())
                .filter(t -> t.name().equalsIgnoreCase(request.getConcept_option()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_IMAGE_THEME));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> conceptOptionMap = new HashMap<>();
        conceptOptionMap.put("theme_template", theme.name().toLowerCase());
        conceptOptionMap.put("product_size", "auto");
        conceptOptionMap.put("num_results", 4);
        String conceptOptionJson = objectMapper.writeValueAsString(conceptOptionMap);

        log.info("전송된 concept_option JSON: {}", conceptOptionJson);

        FileSystemResource fileResource = new FileSystemResource(tempFile.toFile());
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("image", fileResource);
        formData.add("username", USERNAME);
        formData.add("gen_type", "concept");
        formData.add("output_w", 1080);
        formData.add("output_h", 1080);
        formData.add("concept_option", conceptOptionJson);

        String jsonResponse = draphArtWebClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return handleBgGenerateResponse(jsonResponse, image.getUser().getId());
    }

    // 배경 생성 (커스텀)
    @Transactional
    public List<BgGenerateResponse> generateCustomBg(Long imageId, BgCustomGenerateRequest request) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        Path tempFile = Files.createTempFile("bg_processed_", ".png");
        try (InputStream in = new URL(image.getProcessedImage()).openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        String prompt = request.getCustomPrompt();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> conceptOptionMap = new HashMap<>();
        conceptOptionMap.put("theme_template", "custom");
        conceptOptionMap.put("custom_prompt", prompt);
        conceptOptionMap.put("product_size", "auto");
        conceptOptionMap.put("num_results", 4);
        String conceptOptionJson = objectMapper.writeValueAsString(conceptOptionMap);

        log.info("전송된 concept_option JSON: {}", conceptOptionJson);

        FileSystemResource fileResource = new FileSystemResource(tempFile.toFile());
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("image", fileResource);
        formData.add("username", USERNAME);
        formData.add("gen_type", "concept");
        formData.add("output_w", 1080);
        formData.add("output_h", 1080);
        formData.add("concept_option", conceptOptionJson);

        String jsonResponse = draphArtWebClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return handleBgGenerateResponse(jsonResponse, image.getUser().getId());
    }

    // 최종 이미지 선택
    @Transactional
    public FinalImageResponse selectFinalImage(Long imageId, FinalImageRequest request) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        File tempFile = new File(tempDir, request.getFileName());
        log.info("생성파일: {}", tempFile);
        String fileName = "final_" + imageId + ".png";
        File finalFile = new File(finalDir, fileName);
        log.info("최종파일: {}", finalFile);

        fileStorageService.moveFile(tempFile, finalFile);
        log.info("파일 이동 완료: {} -> {}", tempFile.getAbsolutePath(), finalFile.getAbsolutePath());
        fileStorageService.cleanUpTempDir();

        String finalImageUrl = finalUrl + fileName;
        image.setFinalImage(finalImageUrl);
        imageRepository.save(image);
        return new FinalImageResponse(image.getId(), finalImageUrl);
    }

    // 생성된 이미지 저장
    @Transactional
    public List<BgGenerateResponse> handleBgGenerateResponse(String jsonResponse, Long userId) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            throw new BusinessException(ErrorCode.DRAPH_ART_EMPTY_RESPONSE);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> base64List;

        try {
            base64List = objectMapper.readValue(jsonResponse, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DRAPH_ART_RESPONSE_PROCESSING_FAILED);
        }

        if (base64List.isEmpty()) {
            throw new BusinessException(ErrorCode.DRAPH_ART_EMPTY_RESPONSE);
        }

        List<BgGenerateResponse> responseList = new ArrayList<>();

        for (int i = 0; i < base64List.size(); i++) {
            String base64 = base64List.get(i);
            try {
                String outputImagePath = fileStorageService.saveBase64Image(base64, String.valueOf(userId),"temp-images");
                responseList.add(new BgGenerateResponse(outputImagePath));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.DRAPH_ART_RESPONSE_PROCESSING_FAILED);
            }
        }
        return responseList;
    }

    private BgRemoveResponse toBgRemoveResponse(Image image) {
        return new BgRemoveResponse(
                image.getId(),
                image.getUser().getId(),
                image.getProcessedImage()
        );
    }
}
