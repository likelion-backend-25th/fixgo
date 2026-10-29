package net.likelion.bebc25.fixgo.payment.integration;

public record ConfirmedRepairCharge(
        Long asRequestId,
        Long finalAmount
) {}