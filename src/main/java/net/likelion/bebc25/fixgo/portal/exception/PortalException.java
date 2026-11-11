package net.likelion.bebc25.fixgo.portal.exception;

import net.likelion.bebc25.fixgo.global.exception.BusinessException;

public class PortalException extends BusinessException {
    public PortalException(PortalErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public PortalErrorCode getErrorCode() {
        return (PortalErrorCode) super.getErrorCode();
    }
}