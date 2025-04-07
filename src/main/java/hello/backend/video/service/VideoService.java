package hello.backend.video.service;

import ai.fal.client.FalClient;
import ai.fal.client.Output;
import ai.fal.client.SubscribeOptions;
import ai.fal.client.queue.QueueStatus;
import com.google.gson.JsonObject;
import hello.backend.ai.deepseek.service.DeepSeekService;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import hello.backend.video.domain.Video;
import hello.backend.video.dto.*;
import hello.backend.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FalClient falClient;
    private final DeepSeekService deepSeekService;
    private final VideoService videoService;


    //text-to-video 생성
    @Transactional
    public JsonObject createTextToVideo(TextToVideoRequest request) {

        String finalprompt = deepSeekService.textTransFormScript(request.getPrompt());

        Map<String, Object> input = Map.of(
                "prompt", finalprompt,
                "aspect_ratio", request.getAspect_ratio(),
                "duration", request.getDuration()
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

        Output<JsonObject> output = falClient.subscribe("fal-ai/veo2", options);

        JsonObject result = output.getData();

        return result;
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

        Output<JsonObject> output = falClient.subscribe("fal-ai/kling-video/v1.6/pro/image-to-video", options);

        JsonObject result = output.getData();
        String videoUrl = videoService.extractVideoUrl(result);

        return ImageToVideoResponse.builder()
                .videoUrl(videoUrl)
                .duration(request.getDuration())
                .aspect_ratio(request.getAspect_ratio())
                .createdAt(LocalDateTime.now())
                .build();
    }

    //video 저장
    @Transactional
    public SaveResponse saveVideo(Long userId, SaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

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

    //예외처리
    @Transactional
    public String extractVideoUrl(JsonObject result) {
        if (result == null || !result.has("video")) {
            throw new IllegalStateException("Fal 응답에 video 필드 없음: " + result);
        }

        JsonObject videoObj = result.getAsJsonObject("video");

        if (!videoObj.has("url")) {
            throw new IllegalStateException("Fal 응답의 video 객체에 url 없음: " + result);
        }

        return videoObj.get("url").getAsString();
    }

}