package hello.backend.image.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.backend.exception.BadRequestException;
import hello.backend.exception.NotFoundException;
import hello.backend.image.domain.Image;
import hello.backend.image.dto.BgRemoveResponse;
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
import java.util.Base64;
import java.util.List;

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

    // 이미지 배경 제거
    @Transactional
    public BgRemoveResponse removeBg(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("해당 이미지가 존재하지 않습니다."));

        String uploadImagePath = image.getOriginalImage();
        String outputImagePath = Paths.get(bgRemoveDir, "processed_" + image.getId() + ".png").toString();

        FileSystemResource fileResource = new FileSystemResource(new File(uploadImagePath));
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("username", USERNAME);
        formData.add("gen_type", "remove_bg");
        formData.add("image", fileResource);

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

    private BgRemoveResponse toBgRemoveResponse(Image image) {
        return new BgRemoveResponse(
                image.getId(),
                image.getUser().getId(),
                image.getProcessedImage()
        );
    }
}
