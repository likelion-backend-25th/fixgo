package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentAttemptStatus {
    READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, FAILED;

    /**
     * 현재 상태에서 다음 상태로 바뀔 수 있는지 판단한다.
     * <p>
     * 카드:     READY → IN_PROGRESS → DONE
     * 가상계좌: READY → IN_PROGRESS → WAITING_FOR_DEPOSIT → DONE
     * 현금:     READY → DONE (승인 준비를 거치지 않고 직원이 바로 완료 처리)
     * 실패:     READY, IN_PROGRESS, WAITING_FOR_DEPOSIT → FAILED (승인 거절, 입금 기한 만료 등)
     * <p>
     * DONE과 FAILED는 끝난 상태라 다른 상태로 바뀌지 않는다.
     * 환불은 attempt 상태가 아니라 청구서 상태와 환불 기록으로 관리한다.
     */
    public boolean canTransitTo(PaymentAttemptStatus next) {
        return switch (this) {
            case READY -> next == DONE || next == IN_PROGRESS || next == FAILED;
            case IN_PROGRESS -> next == WAITING_FOR_DEPOSIT || next == DONE || next == FAILED;
            case WAITING_FOR_DEPOSIT -> next == DONE || next == FAILED;
            case DONE, FAILED -> false;
        };
    }

    /**
     * 결제가 진행 중인 상태인지. 진행 중인 시도가 있으면 같은 청구서로 새 결제를 시작할 수 없다(이중 결제 방지).
     */
    public boolean blocksNewAttempt() {
        return this == IN_PROGRESS || this == WAITING_FOR_DEPOSIT || this == DONE;
    }
}
