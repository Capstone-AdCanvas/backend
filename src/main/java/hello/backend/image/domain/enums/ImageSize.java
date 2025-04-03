package hello.backend.image.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageSize {
    PX_1080X1350(1080, 1350, "4:5"),
    PX_1080X1080(1080, 1080, "1:1"),
    PX_1080X1920(1080, 1920, "9:16");

    private final int width;
    private final int height;
    private final String ratio;

    public static ImageSize fromRatio(String ratio) {
        for (ImageSize size : ImageSize.values()) {
            if (size.getRatio().equals(ratio)) {
                return size;
            }
        }
        throw new IllegalArgumentException("값을 찾을 수 없습니다." + ratio);
    }
}
