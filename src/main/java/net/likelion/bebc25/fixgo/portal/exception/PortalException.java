package net.likelion.bebc25.fixgo.portal.exception;

import lombok.Getter;

@Getter
public class PortalException extends RuntimeException {
    private final PortalErrorCode errorCode;

    public PortalException(PortalErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}