package net.likelion.bebc25.fixgo.repair.exception;

import lombok.Getter;
import net.likelion.bebc25.fixgo.global.exception.BusinessException;

@Getter
public class RepairException extends BusinessException {

    public RepairException(RepairErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public RepairErrorCode getErrorCode() {
        return (RepairErrorCode) super.getErrorCode();
    }
}