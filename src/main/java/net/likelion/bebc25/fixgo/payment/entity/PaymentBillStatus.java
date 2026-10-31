package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentBillStatus {
    UNPAID, PAID, PARTIALLY_REFUNDED, REFUNDED;

    public boolean canTransitTo(PaymentBillStatus next) {
        return switch (this) {
            case UNPAID -> next == PAID;
            case PAID -> next == PARTIALLY_REFUNDED || next == REFUNDED;
            case PARTIALLY_REFUNDED -> next == PARTIALLY_REFUNDED || next == REFUNDED;
            case REFUNDED -> false;
        };
    }
}
