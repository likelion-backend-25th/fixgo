package net.likelion.bebc25.fixgo.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.payment.entity.*;

import java.time.LocalDateTime;

public class PaymentDto {

    /**
     * 결제 시작 응답.
     * orderId와 amount는 승인 시 서버가 DB 값과 대조해 검증한다.
     */
    public record StartPaymentResponse(
            String orderId,
            Long amount,
            String orderName
    ) {
        public static StartPaymentResponse from(PaymentAttempt attempt) {
            return new StartPaymentResponse(
                    attempt.getOrderId(),
                    attempt.getAmount(),
                    attempt.getPaymentBill().getType().getOrderName()
            );
        }
    }

    /**
     * 결제 승인 요청. 프론트는 토스 결제창의 successUrl로 돌아올 때
     * 쿼리 파라미터로 받은 세 값을 그대로 보낸다.
     * <p>
     * 서버는 이 값을 신뢰하지 않는다.
     * paymentKey는 토스 승인 응답의 값을 저장한다.
     */
    public record ConfirmPaymentRequest(
            @NotBlank(message = "주문번호는 필수입니다.")
            String orderId,
            @NotBlank(message = "결제키는 필수입니다.")
            String paymentKey,
            @NotNull(message = "금액은 필수입니다.")
            Long amount
    ) {}

    /**
     * 결제 완료된 청구서의 전액 환불 요청.
     * 카드 환불은 refundAccount를 생략할 수 있다.
     * 가상계좌 환불은 은행 코드, 계좌번호, 예금주가 필요하다.
     * 환불 금액과 결제수단은 서버에 저장된 결제 정보를 사용한다.
     */
    public record RefundPaymentRequest(
            @NotBlank(message = "환불 사유는 필수입니다.")
            String cancelReason,

            @Valid
            RefundAccountRequest refundAccount
    ) {}

    /**
     * 가상계좌 환불금을 받을 계좌 정보.
     *
     * @param bank          은행 코드
     * @param accountNumber 환불받을 계좌번호
     * @param holderName    예금주명
     */
    public record RefundAccountRequest(
            @NotBlank(message = "은행 코드는 필수입니다.")
            String bank,

            @NotBlank(message = "계좌번호는 필수입니다.")
            String accountNumber,

            @NotBlank(message = "예금주명은 필수입니다.")
            String holderName
    ) {
        public PaymentInternalDto.RefundAccount toInternal() {
            return new PaymentInternalDto.RefundAccount(
                    bank, accountNumber, holderName
            );
        }
    }

    /**
     * 포털 결제 영역에 보여 줄 현재 결제 상태.
     * 상태에 따라 채워지는 필드가 다르다. 프론트는 status로 화면을 고르고, 해당 화면에서 쓰는 필드만 읽는다.
     * <p>
     * NOT_ISSUED          : status
     * UNPAID              : status, orderName, amount
     * WAITING_FOR_DEPOSIT : status, orderName, amount, paymentMethod, virtualAccount
     * PAID                : status, orderName, amount, paymentMethod, paidAt
     * <p>
     * 나머지 필드는 null이다. 필드 목록을 상태와 상관없이 같게 유지해 프론트가 하나의 타입으로 받을 수 있게 한다.
     */
    public record PaymentSummaryResponse(
            Status status,
            String orderName,
            Long amount,
            PaymentMethod paymentMethod,
            LocalDateTime paidAt,
            VirtualAccountInfo virtualAccount
    ) {
        public static PaymentSummaryResponse notIssued() {
            return new PaymentSummaryResponse(
                    Status.NOT_ISSUED,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        public static PaymentSummaryResponse unpaid(PaymentBill bill) {
            return new PaymentSummaryResponse(
                    Status.UNPAID,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    null,
                    null,
                    null
            );
        }

        public static PaymentSummaryResponse paid(PaymentBill bill, PaymentAttempt doneAttempt) {
            return new PaymentSummaryResponse(
                    Status.PAID,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    doneAttempt.getMethod(),
                    bill.getPaidAt(),
                    null
            );
        }

        public static PaymentSummaryResponse waitingForDeposit(
                PaymentBill bill,
                PaymentAttempt attempt,
                VirtualAccount virtualAccount
        ) {
            return new PaymentSummaryResponse(
                    Status.WAITING_FOR_DEPOSIT,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    attempt.getMethod(),
                    null,
                    new VirtualAccountInfo(virtualAccount.getBankCode(),
                                           virtualAccount.getAccountNumber(),
                                           virtualAccount.getDueAt())
            );
        }

        public static PaymentSummaryResponse refunded(PaymentBill bill, PaymentAttempt doneAttempt) {
            return new PaymentSummaryResponse(
                    Status.REFUNDED,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    doneAttempt.getMethod(),
                    bill.getPaidAt(),
                    null
            );
        }

        public enum Status {NOT_ISSUED, UNPAID, WAITING_FOR_DEPOSIT, PAID, REFUNDED}

        /**
         * 가상계좌 입금 안내 정보. 고객이 입금할 때 필요한 값만 담는다.
         */
        public record VirtualAccountInfo(
                String bankName,
                String accountNumber,
                LocalDateTime dueAt
        ) {}


    }

    public record PaymentDetailResponse(
            Long billId,
            ReceptionInfo reception,
            Long amount,
            PaymentBillStatus status,
            PaymentAttemptStatus paymentStatus,
            PaymentMethod paymentMethod,
            AccountInfo virtualAccount,
            PaymentHistoryItem paymentHistory,
            RefundHistoryItem refundHistory,
            boolean refundable
    ) {}

    public record ReceptionInfo(
            String asReceptionNumber, String customerName,
            String productName, String symptom, String symptomDetail
    ) {}

    public record AccountInfo(String bankCode, String accountNumber, LocalDateTime dueAt) {}

    public record PaymentHistoryItem(
            Long attemptId, String orderId, Long amount,
            PaymentMethod paymentMethod, LocalDateTime approvedAt
    ) {
        public static PaymentHistoryItem from(PaymentAttempt attempt) {
            return new PaymentHistoryItem(attempt.getId(), attempt.getOrderId(),
                                          attempt.getAmount(), attempt.getMethod(), attempt.getApprovedAt());
        }
    }

    public record RefundHistoryItem(
            Long refundId, Long amount, String cancelReason,
            PaymentRefundStatus status, LocalDateTime canceledAt
    ) {
        public static RefundHistoryItem from(PaymentRefund refund) {
            return new RefundHistoryItem(refund.getId(), refund.getCancelAmount(),
                                         refund.getCancelReason(), refund.getStatus(), refund.getCanceledAt());
        }
    }

    public record PaymentListResponse(
            Long billId,
            String asReceptionNumber,
            String customerName,
            Long amount,
            PaymentBillStatus status,
            PaymentMethod paymentMethod

    ) {}


}
