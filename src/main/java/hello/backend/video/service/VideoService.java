package hello.backend.video.service;

import ai.fal.client.FalClient;
import ai.fal.client.Output;
import ai.fal.client.SubscribeOptions;
import ai.fal.client.queue.QueueStatus;
import com.google.gson.JsonObject;
import hello.backend.ai.deepseek.service.DeepSeekService;
import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import hello.backend.video.domain.Video;
import hello.backend.video.dto.*;
import hello.backend.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ai.fal.client.exception.FalException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FalClient falClient;
    private final DeepSeekService deepSeekService;

    //image-to-video(kling 모델) 생성
    @Transactional
    public TextToVideoResponse createTextToVideo(TextToVideoRequest request) {
        String finalprompt = deepSeekService.textTransFormScript(request.getPrompt());

        Map<String, Object> input = Map.of(
                "prompt", finalprompt,
                "duration", request.getDuration(),
                "aspect_ratio", request.getAspect_ratio(),
                "negative_prompt", "blur, distort, and low quality",
                "cfg_scale", 0.5
        );

        SubscribeOptions<JsonObject> options = SubscribeOptions.<JsonObject>builder()
                .input(input)
                .logs(true)
                .resultType(JsonObject.class)
                .onQueueUpdate(update -> {
                    if (update instanceof QueueStatus.InProgress progress) {
                        System.out.println(progress.getLogs());
                    }
                })
                .build();

        try {
            Output<JsonObject> output = falClient.subscribe("fal-ai/kling-video/v1.6/pro/text-to-video", options);

            JsonObject result = output.getData();
            String videoUrl = extractVideoUrl(result);

            return TextToVideoResponse.builder()
                    .videoUrl(videoUrl)
                    .duration(request.getDuration())
                    .aspect_ratio(request.getAspect_ratio())
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (FalException e) {
            log.error("🔥 Fal 예외 발생: {}", e.getMessage(), e);
            String msg = e.getMessage();

            if (msg != null) {
                if (msg.contains("400")) {
                    throw new BusinessException(ErrorCode.FAL_INPUT_INVALID, "입력값이 올바르지 않습니다.");
                } else if (msg.contains("401")) {
                    throw new BusinessException(ErrorCode.FAL_UNAUTHORIZED, "인증이 필요하거나 인증에 실패했습니다.");
                } else if (msg.contains("404")) {
                    throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");
                } else if (msg.contains("422")) {
                    throw new BusinessException(ErrorCode.FAL_CONTENT_VIOLATION, "정책 위반 콘텐츠입니다.");
                } else if (msg.contains("504") || msg.contains("generation_timeout")) {
                    throw new BusinessException(ErrorCode.FAL_GENERATION_TIMEOUT, "영상 생성이 너무 오래 걸렸습니다.");
                } else if (msg.contains("500") || msg.contains("image_load_error")) {
                    throw new BusinessException(ErrorCode.FAL_INTERNAL_ERROR, "AI 서버 내부 오류입니다.");
                }
            }
            throw new BusinessException(ErrorCode.FAL_INTERNAL_ERROR, "Fal 예외 발생: " + msg);
        }
    }

    //image-to-video(kling 모델) 생성
    @Transactional
    public ImageToVideoResponse createImageToVideo(ImageToVideoRequest request) {
        String finalprompt = deepSeekService.textTransFormScript(request.getPrompt());

        Map<String, Object> input = Map.of(
                "prompt", finalprompt,
                "image_url", request.getImageUrl(),
                "duration", request.getDuration(),
                "aspect_ratio", request.getAspect_ratio(),
                "negative_prompt", "blur, distort, and low quality",
                "cfg_scale", 0.5
        );

        SubscribeOptions<JsonObject> options = SubscribeOptions.<JsonObject>builder()
                .input(input)
                .logs(true)
                .resultType(JsonObject.class)
                .onQueueUpdate(update -> {
                    if (update instanceof QueueStatus.InProgress progress) {
                        System.out.println(progress.getLogs());
                    }
                })
                .build();

        try {
            Output<JsonObject> output = falClient.subscribe("fal-ai/kling-video/v1.6/pro/image-to-video", options);

            JsonObject result = output.getData();
            String videoUrl = extractVideoUrl(result);

            return ImageToVideoResponse.builder()
                    .videoUrl(videoUrl)
                    .duration(request.getDuration())
                    .aspect_ratio(request.getAspect_ratio())
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (FalException e) {
            log.error("🔥 Fal 예외 발생: {}", e.getMessage(), e);
            String msg = e.getMessage();

            if (msg != null) {
                if (msg.contains("400")) {
                    throw new BusinessException(ErrorCode.FAL_INPUT_INVALID, "입력값이 올바르지 않습니다.");
                } else if (msg.contains("401")) {
                    throw new BusinessException(ErrorCode.FAL_UNAUTHORIZED, "인증이 필요하거나 인증에 실패했습니다.");
                } else if (msg.contains("404")) {
                    throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");
                } else if (msg.contains("422")) {
                    throw new BusinessException(ErrorCode.FAL_CONTENT_VIOLATION, "정책 위반 콘텐츠입니다.");
                } else if (msg.contains("504") || msg.contains("generation_timeout")) {
                    throw new BusinessException(ErrorCode.FAL_GENERATION_TIMEOUT, "영상 생성이 너무 오래 걸렸습니다.");
                } else if (msg.contains("500") || msg.contains("image_load_error")) {
                    throw new BusinessException(ErrorCode.FAL_INTERNAL_ERROR, "AI 서버 내부 오류입니다.");
                }
            }
            throw new BusinessException(ErrorCode.FAL_INTERNAL_ERROR, "Fal 예외 발생: " + msg);
        }
    }

    //video 저장
    @Transactional
    public SaveResponse saveVideo(Long userId, SaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        Video video = Video.builder()
                .name(request.getName())
                .finalVideo(request.getVideoUrl())
                .aspectRatio(request.getAspectRatio())
                .duration(request.getDuration())
                .createAt(request.getCreatedAt())
                .user(user)
                .build();

        videoRepository.save(video);

        return SaveResponse.builder()
                .userId(userId)
                .name(request.getName())
                .videoUrl(request.getVideoUrl())
                .aspectRatio(request.getAspectRatio())
                .duration(request.getDuration())
                .createdAt(request.getCreatedAt())
                .build();
    }

    //----------------------------------------------
    //예외처리
    @Transactional
    public String extractVideoUrl(JsonObject result) {
        if (result == null || !result.has("video")) {
            throw new BusinessException(ErrorCode.FAL_INTERNAL_ERROR, "Fal 응답에 video 필드 없음: " + result);
        }

        JsonObject videoObj = result.getAsJsonObject("video");

        if (!videoObj.has("url")) {
            throw new BusinessException(ErrorCode.FAL_INTERNAL_ERROR, "Fal 응답의 video 객체에 url 없음: " + result);
        }

        return videoObj.get("url").getAsString();
    }
}