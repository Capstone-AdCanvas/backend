package hello.backend.video.service;

import ai.fal.client.FalClient;
import ai.fal.client.Output;
import ai.fal.client.queue.QueueResultOptions;
import ai.fal.client.queue.QueueStatus;
import ai.fal.client.queue.QueueStatusOptions;
import ai.fal.client.queue.QueueSubmitOptions;
import com.github.benmanes.caffeine.cache.Cache;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FalClient falClient;
    private final DeepSeekService deepSeekService;
    private final Cache<String, TextToVideoRequest> requestTextCache;
    private final Cache<String, ImageToVideoRequest> requestImageCache;

    //image-to-video(kling 모델) 생성 - 비동기 큐
    @Transactional
    public QueueStatus.InQueue createTextToVideo(TextToVideoRequest request) {
        String finalprompt = deepSeekService.imageTransFormScript(request.getPrompt());

        Map<String, Object> input = Map.of(
                "prompt", finalprompt,
                "duration", request.getDuration(),
                "aspect_ratio", request.getAspect_ratio()
        );

        QueueSubmitOptions options = QueueSubmitOptions.builder()
                .input(input)
                .build();
        try {
            QueueStatus.InQueue inQueue = falClient.queue().submit("fal-ai/kling-video/v1.6/pro/text-to-video", options);
            String requestId = inQueue.getRequestId();
            requestTextCache.put(requestId, request);

            return inQueue;
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

    //image-to-video(kling 모델) 생성 - 비동기 큐
    @Transactional
    public QueueStatus.InQueue createImageToVideo(ImageToVideoRequest request) {
        String finalprompt = deepSeekService.imageTransFormScript(request.getPrompt());

        Map<String, Object> input = Map.of(
                "prompt", finalprompt,
                "image_url", request.getImageUrl(),
                "duration", request.getDuration(),
                "aspect_ratio", request.getAspect_ratio()
        );

        QueueSubmitOptions options = QueueSubmitOptions.builder()
                .input(input)
                .build();
        try {
            QueueStatus.InQueue inQueue = falClient.queue().submit("fal-ai/kling-video/v1.6/pro/image-to-video", options);
            String requestId = inQueue.getRequestId();
            requestImageCache.put(requestId, request);

            return inQueue;
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
    //--------------------------------------------------------------------------------------------------
    //text-to-video(kling 모델) 조회 - polling
    public TextToVideoResponse getTextToVideo(String requestId) {
        QueueStatusOptions statusOptions = QueueStatusOptions.builder()
                .requestId(requestId)
                .logs(true)
                .build();

        QueueStatus.StatusUpdate status = falClient.queue().status("fal-ai/kling-video/v1.6/pro/text-to-video", statusOptions);
        String resultStatus = statusCheck(status);

        if (!"completed".equals(resultStatus)) {
            return TextToVideoResponse.builder()
                    .status(resultStatus)
                    .build();
        }

        QueueResultOptions<JsonObject> options = QueueResultOptions.<JsonObject>builder()
                .requestId(requestId)
                .resultType(JsonObject.class)
                .build();

        Output<JsonObject> output = falClient.queue().result("fal-ai/kling-video/v1.6/pro/text-to-video", options);
        JsonObject result = output.getData();

        log.info("result 로그: {}",result);

        TextToVideoRequest cacheRequest = Optional.ofNullable(requestTextCache.getIfPresent(requestId))
                .orElseThrow(() -> new BusinessException(ErrorCode.FAL_NOT_FOUND, "요청 정보를 찾을 수 없습니다."));

        return TextToVideoResponse.builder()
                .status(resultStatus)
                .videoUrl(result.getAsJsonObject("video").get("url").getAsString())
                .aspect_ratio(cacheRequest.getAspect_ratio())
                .duration(cacheRequest.getDuration())
                .createdAt(LocalDateTime.now())
                .build();
    }

    //image-to-video(kling 모델) 조회 - polling
    public ImageToVideoResponse getImeageToVideo(String requestId) {
        QueueStatusOptions statusOptions = QueueStatusOptions.builder()
                .requestId(requestId)
                .logs(true)
                .build();

        QueueStatus.StatusUpdate status = falClient.queue().status("fal-ai/kling-video/v1.6/pro/image-to-video", statusOptions);
        String resultStatus = statusCheck(status);

        if (!"completed".equals(resultStatus)) {
            return ImageToVideoResponse.builder()
                    .status(resultStatus)
                    .build();
        }

        QueueResultOptions<JsonObject> options = QueueResultOptions.<JsonObject>builder()
                .requestId(requestId)
                .resultType(JsonObject.class)
                .build();

        Output<JsonObject> output = falClient.queue().result("fal-ai/kling-video/v1.6/pro/image-to-video", options);
        JsonObject result = output.getData();

        log.info("result 로그: {}",result);

        ImageToVideoRequest cacheRequest = Optional.ofNullable(requestImageCache.getIfPresent(requestId))
                .orElseThrow(() -> new BusinessException(ErrorCode.FAL_NOT_FOUND, "요청 정보를 찾을 수 없습니다."));

        return ImageToVideoResponse.builder()
                .status(resultStatus)
                .videoUrl(result.getAsJsonObject("video").get("url").getAsString())
                .aspect_ratio(cacheRequest.getAspect_ratio())
                .duration(cacheRequest.getDuration())
                .createdAt(LocalDateTime.now())
                .build();
    }
    //--------------------------------------------------------------------------------------------------
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
    //--------------------------------------------------------------------------------------------------
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

    @Transactional
    public String statusCheck(QueueStatus.StatusUpdate status) {
        if (status instanceof QueueStatus.InQueue) {
            return "queued";
        } else if (status instanceof QueueStatus.InProgress) {
            return "processing";
        } else if (status instanceof QueueStatus.Completed) {
            return "completed";
        } else {
            throw new BusinessException(ErrorCode.FAL_NOT_FOUND, "알 수 없는 상태입니다.");
        }
    }
}