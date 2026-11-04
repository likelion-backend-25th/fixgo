package net.likelion.bebc25.fixgo.auth.exception;

import lombok.Getter;
import net.likelion.bebc25.fixgo.global.exception.BusinessException;

@Getter
public class EmployeeHasActiveRepairsException extends BusinessException {
    public EmployeeHasActiveRepairsException(AuthErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public AuthErrorCode getErrorCode() {
        return (AuthErrorCode) super.getErrorCode();
    }
}
