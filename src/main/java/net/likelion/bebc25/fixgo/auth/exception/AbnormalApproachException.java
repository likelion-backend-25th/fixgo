package net.likelion.bebc25.fixgo.auth.exception;

import net.likelion.bebc25.fixgo.global.exception.BusinessException;
import net.likelion.bebc25.fixgo.global.exception.ErrorCode;

public class AbnormalApproachException extends BusinessException {
    public AbnormalApproachException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return super.getErrorCode();
    }
}
