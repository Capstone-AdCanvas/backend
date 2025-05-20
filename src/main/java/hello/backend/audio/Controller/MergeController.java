package hello.backend.audio.Controller;

import hello.backend.audio.service.MergeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audios")
@RequiredArgsConstructor
public class MergeController {
    private final MergeService audioService;

    @Operation(summary = "video 음성 적용", description = "영상에 음향처리를 적용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "음성 적용 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값입니다."),
    })
    @GetMapping("/merge")
    public ResponseEntity<Resource> mergeVideoAudio(@RequestParam List<String> videoUrls,@RequestParam(required = false) String tema) throws IOException {
        File file = audioService.mergeVideoAudio(videoUrls, tema);
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
