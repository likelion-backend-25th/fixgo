package net.likelion.bebc25.fixgo.portal.exception;

import net.likelion.bebc25.fixgo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PortalErrorCode implements ErrorCode {
    LINK_NOT_FOUND(HttpStatus.NOT_FOUND),
    LINK_EXPIRED(HttpStatus.GONE),
    LINK_ALREADY_EXISTS(HttpStatus.CONFLICT);

    private final HttpStatus httpStatus;

    PortalErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }
}