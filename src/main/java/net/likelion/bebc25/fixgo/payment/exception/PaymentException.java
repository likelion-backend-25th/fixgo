package net.likelion.bebc25.fixgo.payment.exception;

import lombok.Getter;
import net.likelion.bebc25.fixgo.global.exception.BusinessException;

@Getter
public class PaymentException extends BusinessException {
    public PaymentException(PaymentErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public PaymentErrorCode getErrorCode() {
        return (PaymentErrorCode) super.getErrorCode();
    }
}
