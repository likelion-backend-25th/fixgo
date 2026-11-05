package net.likelion.bebc25.fixgo.payment.exception;

import net.likelion.bebc25.fixgo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements ErrorCode {
    BILL_ALREADY_EXISTS(HttpStatus.CONFLICT),
    ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND),
    ATTEMPT_NOT_READY(HttpStatus.CONFLICT),
    BILL_NOT_PAYABLE(HttpStatus.CONFLICT),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST),
    PAYMENT_IN_PROGRESS(HttpStatus.CONFLICT),
    BILL_NOT_FOUND(HttpStatus.NOT_FOUND),
    SECRET_MISMATCH(HttpStatus.CONFLICT),
    VIRTUAL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND),
    VIRTUAL_ACCOUNT_NOT_CANCELABLE(HttpStatus.CONFLICT),
    BILL_NOT_REFUNDABLE(HttpStatus.CONFLICT);

    // 이 실패를 응답할 때의 HTTP 상태
    private final HttpStatus httpStatus;

    PaymentErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }
}
