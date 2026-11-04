package net.likelion.bebc25.fixgo.auth.exception;

import net.likelion.bebc25.fixgo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    ABNORMAL_APPROACH(HttpStatus.FORBIDDEN),
    EMPLOYEE_HAS_ACTIVE_REPAIRS(HttpStatus.CONFLICT);

    private final HttpStatus status;

    AuthErrorCode(HttpStatus status) {
        this.status = status;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
