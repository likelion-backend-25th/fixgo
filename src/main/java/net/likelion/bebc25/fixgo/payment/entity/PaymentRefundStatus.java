package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentRefundStatus {
    IN_PROGRESS, DONE, FAILED;

    public boolean canTransitTo(PaymentRefundStatus next) {
        return switch (this) {
            case IN_PROGRESS -> next == DONE || next == FAILED;
            case DONE, FAILED -> false;
        };
    }
}
