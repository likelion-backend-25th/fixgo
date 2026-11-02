package net.likelion.bebc25.fixgo.payment.exception;

public enum PaymentErrorCode {
    ATTEMPT_NOT_FOUND(false),
    ATTEMPT_NOT_READY(false),
    BILL_NOT_PAYABLE(true),
    AMOUNT_MISMATCH(true),
    PAYMENT_IN_PROGRESS(false),
    BILL_NOT_FOUND(false);

    // 이 실패가 발생하면 attempt를 FAILED로 기록해야 하는가
    private final boolean recordAsFailure;

    PaymentErrorCode(boolean recordAsFailure) {
        this.recordAsFailure = recordAsFailure;
    }

    public boolean shouldRecordAsFailure() {
        return recordAsFailure;
    }
}
