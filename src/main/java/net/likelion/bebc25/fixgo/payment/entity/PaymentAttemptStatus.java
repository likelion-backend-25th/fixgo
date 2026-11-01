package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentAttemptStatus {
    READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, FAILED;

    public boolean canTransitTo(PaymentAttemptStatus next) {
        return switch (this) {
            case READY -> next == DONE || next == IN_PROGRESS || next == WAITING_FOR_DEPOSIT || next == FAILED;
            case WAITING_FOR_DEPOSIT, IN_PROGRESS -> next == DONE || next == FAILED;
            case DONE, FAILED -> false;
        };
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS || this == WAITING_FOR_DEPOSIT;
    }
}
