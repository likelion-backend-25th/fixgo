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
    BILL_NOT_REFUNDABLE(HttpStatus.CONFLICT),
    // 토스가 승인을 거절함(4xx). attempt는 FAILED로 기록되며, 새 결제로 다시 시도할 수 있다.
    TOSS_REJECTED(HttpStatus.BAD_REQUEST),
    // 토스 응답을 받지 못했거나(타임아웃) 토스 서버 오류(5xx)라 결과를 알 수 없음. 스케줄러가 정리한다.
    PAYMENT_PENDING(HttpStatus.GATEWAY_TIMEOUT);

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
