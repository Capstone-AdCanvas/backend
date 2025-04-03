package hello.backend.video.service;

import ai.fal.client.FalClient;
import ai.fal.client.Output;
import ai.fal.client.SubscribeOptions;
import ai.fal.client.queue.QueueStatus;
import com.google.gson.JsonObject;
import hello.backend.user.domain.User;
import hello.backend.user.repository.UserRepository;
import hello.backend.video.domain.Video;
import hello.backend.video.dto.ImageToVideoRequest;
import hello.backend.video.dto.SaveResponse;
import hello.backend.video.dto.TextToVideoRequest;
import hello.backend.video.dto.SaveRequest;
import hello.backend.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FalClient falClient;


    //text-to-video 생성
    @Transactional
    public JsonObject createTextToVideo(TextToVideoRequest request, String finalprompt) {

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

    //image-to-video 생성
    @Transactional
    public JsonObject createImageToVideo(ImageToVideoRequest request, String finalprompt) {

        Map<String, Object> input = Map.of(
                "image_url", request.getImageUrl(),
                "prompt", finalprompt,
                "resolution", "1080p",
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

        Output<JsonObject> output = falClient.subscribe("fal-ai/pika/v2.1/image-to-video", options);

        JsonObject result = output.getData();

        return result;
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

        return new SaveResponse(
                user.getId(),
                request.getName(),
                request.getVideoUrl(),
                request.getAspectRatio(),
                request.getDuration(),
                request.getCreatedAt()
        );
    }
}