package net.likelion.bebc25.fixgo.statistics.dto;

import java.time.LocalDate;

public record PaymentStatisticsResponse(LocalDate startDate, LocalDate endDate, Long totalPaidAmount,
                                        Long totalRefundAmount, Long netCollectedAmount, Long unpaidCount) {}