package net.likelion.bebc25.fixgo.payment.exception;

import net.likelion.bebc25.fixgo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements ErrorCode {
    BILL_ALREADY_EXISTS(false, HttpStatus.CONFLICT),
    ATTEMPT_NOT_FOUND(false, HttpStatus.NOT_FOUND),
    ATTEMPT_NOT_READY(false, HttpStatus.CONFLICT),
    BILL_NOT_PAYABLE(true, HttpStatus.CONFLICT),
    AMOUNT_MISMATCH(true, HttpStatus.BAD_REQUEST),
    PAYMENT_IN_PROGRESS(false, HttpStatus.CONFLICT),
    BILL_NOT_FOUND(false, HttpStatus.NOT_FOUND),
    SECRET_MISMATCH(false, HttpStatus.CONFLICT),
    VIRTUAL_ACCOUNT_NOT_FOUND(false, HttpStatus.NOT_FOUND),
    VIRTUAL_ACCOUNT_NOT_CANCELABLE(false, HttpStatus.CONFLICT),
    BILL_NOT_REFUNDABLE(false, HttpStatus.CONFLICT);

    // 이 실패가 발생하면 attempt를 FAILED로 기록해야 하는가
    private final boolean recordAsFailure;
    // 이 실패를 응답할 때의 HTTP 상태
    private final HttpStatus httpStatus;

    PaymentErrorCode(boolean recordAsFailure, HttpStatus httpStatus) {
        this.recordAsFailure = recordAsFailure;
        this.httpStatus = httpStatus;
    }

    public boolean shouldRecordAsFailure() {
        return recordAsFailure;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }
}
