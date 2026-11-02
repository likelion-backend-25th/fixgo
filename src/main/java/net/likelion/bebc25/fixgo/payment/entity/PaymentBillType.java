package net.likelion.bebc25.fixgo.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentBillType {
    REPAIR("A/S 수리비"),
    INSPECTION("A/S 점검비");

    private final String orderName;

    PaymentBillType(String orderName) {
        this.orderName = orderName;
    }
}
