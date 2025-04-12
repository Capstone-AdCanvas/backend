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
    FAL_GENERATION_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "FAL_504", "영상 생성이 너무 오래 걸렸습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
