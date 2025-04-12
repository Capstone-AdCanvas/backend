package hello.backend;

import hello.backend.error.ErrorResponse;
import hello.backend.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), e.getErrors());

        if (e.getErrorCode().getHttpStatus().is5xxServerError()) {
            log.error(e.getMessage(), e);
        }
        else {
            log.warn(e.getMessage(), e);
        }

        return new ResponseEntity<>(errorResponse, e.getErrorCode().getHttpStatus());
    }
}
