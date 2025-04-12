package hello.backend.image.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.backend.error.exception.user.BadRequestException;
import hello.backend.error.exception.user.NotFoundException;
import hello.backend.image.domain.Image;
import hello.backend.image.domain.enums.ImageSize;
import hello.backend.image.domain.enums.ImageTheme;
import hello.backend.image.dto.*;
import hello.backend.image.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageBgService {

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final WebClient webClient;

    @Value("${DRAPH_ART_USERNAME}")
    private String USERNAME;

    @Value("${file.bgremove-dir}")
    private String bgRemoveDir;

    @Value("${file.temp-dir}")
    private String tempDir;

    @Value("${file.final-dir}")
    private String finalDir;

    // 이미지 배경 제거
    @Transactional
    public BgRemoveResponse removeBg(Long imageId) throws JsonProcessingException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("해당 이미지가 존재하지 않습니다."));

        String uploadImagePath = image.getOriginalImage();
        String outputImagePath = Paths.get(bgRemoveDir, String.format("processed_%d.png", image.getId())).toString();

        Map<String, Object> conceptOptionMap = new HashMap<>();
        conceptOptionMap.put("product_size", "auto");
        String conceptOptionJson = new ObjectMapper().writeValueAsString(conceptOptionMap);

        FileSystemResource fileResource = new FileSystemResource(new File(uploadImagePath));
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("username", USERNAME);
        formData.add("gen_type", "remove_bg");
        formData.add("image", fileResource);
        formData.add("concept_option", conceptOptionJson);

        String base64ArrayString = webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> base64List = objectMapper.readValue(base64ArrayString, new TypeReference<List<String>>() {});

            if (base64List.isEmpty()) {
                throw new BadRequestException("배경 제거 이미지 응답이 비어있습니다.");
            }

            String base64 = base64List.get(0);
            byte[] imageBytes = Base64.getDecoder().decode(base64);

            fileStorageService.saveBgRemoveFile(imageBytes, outputImagePath);
            image.setProcessedImage(outputImagePath);
            imageRepository.save(image);

            return toBgRemoveResponse(image);
        } catch (Exception e) {
            log.error("배경 제거 처리 중 예외 발생", e);
            throw new BadRequestException("배경 제거 응답 처리 중 오류 발생");
        }
    }

    // 배경 생성
    @Transactional
    public List<BgGenerateResponse> generateBg(Long imageId, BgGenerateRequest request) throws JsonProcessingException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("해당 이미지가 존재하지 않습니다."));

        String uploadImagePath = image.getProcessedImage();

        ImageSize selectedSize = Arrays.stream(ImageSize.values())
                .filter(size -> size.getRatio().equals(request.getRatio()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("유효하지 않은 비율입니다."));

        ImageTheme theme = Arrays.stream(ImageTheme.values())
                .filter(t -> t.name().equalsIgnoreCase(request.getConcept_option()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("유효하지 않은 테마입니다: " + request.getConcept_option()));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> conceptOptionMap = new HashMap<>();
        conceptOptionMap.put("theme_template", theme.name().toLowerCase());
        conceptOptionMap.put("product_size", "auto");
        conceptOptionMap.put("num_results", 4);
        String conceptOptionJson = objectMapper.writeValueAsString(conceptOptionMap);

        log.info("전송된 concept_option JSON: {}", conceptOptionJson);

        FileSystemResource fileResource = new FileSystemResource(new File(uploadImagePath));
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("image", fileResource);
        formData.add("username", USERNAME);
        formData.add("gen_type", "concept");
        formData.add("output_w", selectedSize.getWidth());
        formData.add("output_h", selectedSize.getHeight());
        formData.add("concept_option", conceptOptionJson);

        String jsonResponse = webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return handleBgGenerateResponse(jsonResponse);
    }

    // 배경 생성 (커스텀)
    @Transactional
    public List<BgGenerateResponse> generateCustomBg(Long imageId, BgCustomGenerateRequest request) throws JsonProcessingException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("해당 이미지가 존재하지 않습니다."));

        String uploadImagePath = image.getProcessedImage();
        String prompt = request.getCustomPrompt();
        ImageSize selectedSize = Arrays.stream(ImageSize.values())
                .filter(size -> size.getRatio().equals(request.getRatio()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("유효하지 않은 비율입니다."));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> conceptOptionMap = new HashMap<>();
        conceptOptionMap.put("theme_template", "custom");
        conceptOptionMap.put("custom_prompt", prompt);
        conceptOptionMap.put("product_size", "auto");
        conceptOptionMap.put("num_results", 4);
        String conceptOptionJson = objectMapper.writeValueAsString(conceptOptionMap);

        log.info("전송된 concept_option JSON: {}", conceptOptionJson);

        FileSystemResource fileResource = new FileSystemResource(new File(uploadImagePath));
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("image", fileResource);
        formData.add("username", USERNAME);
        formData.add("gen_type", "concept");
        formData.add("output_w", selectedSize.getWidth());
        formData.add("output_h", selectedSize.getHeight());
        formData.add("concept_option", conceptOptionJson);

        String jsonResponse = webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return handleBgGenerateResponse(jsonResponse);
    }

    // 최종 이미지 선택
    @Transactional
    public FinalImageResponse selectFinalImage(Long imageId, FinalImageRequest request) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("해당 이미지가 존재하지 않습니다."));

        File tempFile = new File(tempDir, request.getFileName());
        String fileName = "final_" + imageId + ".png";
        File finalFile = new File(finalDir, fileName);

        fileStorageService.moveFile(tempFile, finalFile);
        log.info("파일 이동 완료: {} -> {}", tempFile.getAbsolutePath(), finalFile.getAbsolutePath());
        fileStorageService.cleanUpTempDir();

        image.setFinalImage(finalFile.getAbsolutePath());
        imageRepository.save(image);
        return new FinalImageResponse(image.getId(), image.getFinalImage());
    }

    // 생성된 이미지 저장
    @Transactional
    public List<BgGenerateResponse> handleBgGenerateResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            log.error("API 응답이 비어 있음");
            throw new BadRequestException("배경 생성 API 응답이 비어 있습니다.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> base64List;

        try {
            base64List = objectMapper.readValue(jsonResponse, new TypeReference<List<String>>() {});
            log.info("JSON 배열로 파싱된 Base64 리스트 크기: {}", base64List.size());
        } catch (Exception e) {
            log.error("JSON 파싱 실패: 응답이 예상한 포맷이 아님", e);
            throw new BadRequestException("배경 생성 응답을 처리할 수 없습니다.");
        }

        if (base64List.isEmpty()) {
            log.error("배경 생성 응답이 비어있습니다.");
            throw new BadRequestException("배경 생성 응답이 비어있습니다.");
        }

        List<BgGenerateResponse> responseList = new ArrayList<>();

        for (int i = 0; i < base64List.size(); i++) {
            String base64 = base64List.get(i);
            try {
                String outputImagePath = fileStorageService.saveBase64Image(base64);
                responseList.add(new BgGenerateResponse(outputImagePath));
            } catch (IllegalArgumentException e) {
                log.error("Base64 디코딩 실패: index={} data={}", i, base64, e);
                throw new BadRequestException("배경 생성 응답 데이터가 올바르지 않습니다.");
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
