package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentAttemptStatus {
    READY, WAITING_FOR_DEPOSIT, DONE, FAILED;

    public boolean canTransitTo(PaymentAttemptStatus next) {
        return switch (this) {
            case READY -> next == DONE || next == WAITING_FOR_DEPOSIT || next == FAILED;
            case WAITING_FOR_DEPOSIT -> next == DONE || next == FAILED;
            case DONE, FAILED -> false;
        };
    }
}
