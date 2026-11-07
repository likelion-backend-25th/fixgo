package net.likelion.bebc25.fixgo.payment.entity;

public enum PaymentMethod {
    CARD, EASY_PAY, VIRTUAL_ACCOUNT, CASH;

    public static PaymentMethod fromToss(String tossMethod) {
        return switch (tossMethod) {
            case "카드" -> CARD;
            case "간편결제" -> EASY_PAY;
            default -> throw new IllegalArgumentException("지원하지 않는 결제수단: " + tossMethod);
        };
    }
}
