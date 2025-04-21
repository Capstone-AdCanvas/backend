package hello.backend.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    USER_LIST_EMPTY(HttpStatus.NOT_FOUND, "USER_404", "조회할 사용자가 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409", "이미 존재하는 사용자입니다."),
    INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "USER_400", "사용자 입력값이 올바르지 않습니다."),

    //fal.ai
    FAL_INPUT_INVALID(HttpStatus.BAD_REQUEST, "FAL_400", "입력값이 올바르지 않습니다."),
    FAL_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "FAL_401", "인증이 필요하거나 인증에 실패했습니다."),
    FAL_NOT_FOUND(HttpStatus.NOT_FOUND, "FAL_404", "요청한 리소스를 찾을 수 없습니다."),
    FAL_CONTENT_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "FAL_422", "정책 위반 콘텐츠입니다."),
    FAL_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FAL_500", "AI 서버 내부 오류입니다."),
    FAL_GENERATION_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "FAL_504", "영상 생성이 너무 오래 걸렸습니다."),

    // IMAGE
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_404", "이미지를 찾을 수 없습니다."),
    INVALID_IMAGE_RATIO(HttpStatus.NOT_FOUND, "IMAGE_RATIO_404", "유효하지 않은 비율입니다."),
    INVALID_IMAGE_THEME(HttpStatus.NOT_FOUND, "IMAGE_THEME_404", "유효하지 않은 테마입니다."),
    INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, "IMAGE_400", "잘못된 이미지 파일입니다."),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "IMAGE_415", "지원하지 않는 이미지 형식입니다."),

    // DRAPH_ART
    DRAPH_ART_INVALID_INPUT(HttpStatus.BAD_REQUEST, "DRAPH_ART_400", "드랩아트 요청이 올바르지 않습니다."),
    DRAPH_ART_EMPTY_RESPONSE(HttpStatus.BAD_REQUEST, "DRAPH_ART_400_EMPTY", "배경 생성 API 응답이 비어 있습니다."),
    DRAPH_ART_RESPONSE_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "DRAPH_ART_400_RESPONSE", "배경 생성 응답을 처리할 수 없습니다."),
    DRAPH_ART_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DRAPH_ART_500", "드랩아트 생성 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
