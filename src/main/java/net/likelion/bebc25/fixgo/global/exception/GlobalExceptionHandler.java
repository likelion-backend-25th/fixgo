package net.likelion.bebc25.fixgo.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 컨트롤러 밖으로 나온 예외를 HTTP 응답으로 바꾼다.
 * <p>
 * 새 에러를 추가할 때는 이 클래스를 고치지 않는다.
 * ErrorCode를 구현한 enum에 상태 코드를 정하고, BusinessException(또는 그 자식)으로 던지면 된다.
 * <p>
 * TODO: 토스 예외
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 의도해서 던진 예외. 에러 코드가 가진 상태 코드와 메시지를 그대로 응답한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }


    /**
     * 위에서 잡히지 않은 모든 예외. 데이터 이상(IllegalStateException) 등이 여기로 온다.
     * 내부 정보가 담긴 메시지는 고객에게 보내지 않고 로그에만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
}