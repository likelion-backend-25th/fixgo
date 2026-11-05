package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentBillStatus {
    UNPAID, PAID, REFUNDED;

    public boolean canTransitTo(PaymentBillStatus next) {
        return switch (this) {
            case UNPAID -> next == PAID;
            case PAID -> next == REFUNDED;
            case REFUNDED -> false;
        };
    }
}
