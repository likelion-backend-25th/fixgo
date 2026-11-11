package net.likelion.bebc25.fixgo.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.likelion.bebc25.fixgo.payment.entity.*;
import net.likelion.bebc25.fixgo.payment.service.dto.RefundDto;

import java.time.LocalDateTime;

public class PaymentDto {

    /**
     * 결제 시작 응답.
     * orderId와 amount는 승인 시 서버가 DB 값과 대조해 검증한다.
     */
    public record StartPaymentResponse(
            @Schema(description = "주문번호", example = "ORDER-20240101-001")
            String orderId,
            @Schema(description = "결제 금액", example = "50000")
            Long amount,
            @Schema(description = "주문명", example = "수리비")
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
            @Schema(description = "주문번호", example = "ORDER-20240101-001")
            String orderId,
            @NotBlank(message = "결제키는 필수입니다.")
            @Schema(description = "토스 결제키", example = "tgen_xxxxxxxx")
            String paymentKey,
            @NotNull(message = "금액은 필수입니다.")
            @Schema(description = "결제 금액", example = "50000")
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
            @Schema(description = "환불 사유", example = "고객 요청")
            String cancelReason,

            @Valid
            @Schema(description = "가상계좌 환불 계좌 정보(카드 환불 시 생략 가능)")
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
            @Schema(description = "은행 코드", example = "88")
            String bank,

            @NotBlank(message = "계좌번호는 필수입니다.")
            @Schema(description = "환불받을 계좌번호", example = "110123456789")
            String accountNumber,

            @NotBlank(message = "예금주명은 필수입니다.")
            @Schema(description = "예금주명", example = "홍길동")
            String holderName
    ) {
        public RefundDto.RefundAccount toRefundDto() {
            return new RefundDto.RefundAccount(
                    bank, accountNumber, holderName
            );
        }
    }

    /**
     * 포털 결제 영역에 보여 줄 현재 결제 상태.
     * 상태에 따라 채워지는 필드가 다르다. 프론트는 status로 화면을 고르고, 해당 화면에서 쓰는 필드만 읽는다.
     * <p>
     * UNPAID              : status, orderName, amount
     * WAITING_FOR_DEPOSIT : status, orderName, amount, paymentMethod, virtualAccount
     * PAID                : status, orderName, amount, paymentMethod, paidAt
     * <p>
     * 나머지 필드는 null이다. 필드 목록을 상태와 상관없이 같게 유지해 프론트가 하나의 타입으로 받을 수 있게 한다.
     */
    public record CustomerPaymentView(
            @Schema(description = "결제 상태", example = "UNPAID")
            Status status,
            @Schema(description = "주문명", example = "수리비")
            String orderName,
            @Schema(description = "결제 금액", example = "50000")
            Long amount,
            @Schema(description = "결제 수단", example = "CARD")
            PaymentMethod paymentMethod,
            @Schema(description = "결제 완료 시각", example = "2024-01-01T12:00:00")
            LocalDateTime paidAt,
            @Schema(description = "가상계좌 입금 안내 정보")
            VirtualAccountInfo virtualAccount
    ) {
        public static CustomerPaymentView unpaid(PaymentBill bill) {
            return new CustomerPaymentView(
                    Status.UNPAID,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    null,
                    null,
                    null
            );
        }

        public static CustomerPaymentView inProgress(PaymentBill bill) {
            return new CustomerPaymentView(
                    Status.IN_PROGRESS,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    null,
                    null,
                    null
            );
        }

        public static CustomerPaymentView paid(PaymentBill bill, PaymentAttempt doneAttempt) {
            return new CustomerPaymentView(
                    Status.PAID,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    doneAttempt.getMethod(),
                    bill.getPaidAt(),
                    null
            );
        }

        public static CustomerPaymentView waitingForDeposit(
                PaymentBill bill,
                PaymentAttempt attempt,
                VirtualAccount virtualAccount
        ) {
            return new CustomerPaymentView(
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

        public static CustomerPaymentView refunded(PaymentBill bill, PaymentAttempt doneAttempt) {
            return new CustomerPaymentView(
                    Status.REFUNDED,
                    bill.getType().getOrderName(),
                    bill.getAmount(),
                    doneAttempt.getMethod(),
                    bill.getPaidAt(),
                    null
            );
        }

        public enum Status {UNPAID, IN_PROGRESS, WAITING_FOR_DEPOSIT, PAID, REFUNDED}

        /**
         * 가상계좌 입금 안내 정보. 고객이 입금할 때 필요한 값만 담는다.
         */
        public record VirtualAccountInfo(
                @Schema(description = "은행명", example = "신한")
                String bankName,
                @Schema(description = "가상계좌번호", example = "56201234567890")
                String accountNumber,
                @Schema(description = "입금 기한", example = "2024-01-02T23:59:59")
                LocalDateTime dueAt
        ) {}


    }

    public record PaymentDetailResponse(
            @Schema(description = "청구서 ID", example = "1")
            Long billId,
            @Schema(description = "접수 정보")
            ReceptionInfo reception,
            @Schema(description = "청구 금액", example = "50000")
            Long amount,
            @Schema(description = "청구서 상태", example = "PAID")
            PaymentBillStatus status,
            @Schema(description = "결제 시도 상태", example = "DONE")
            PaymentAttemptStatus paymentStatus,
            @Schema(description = "결제 수단", example = "CARD")
            PaymentMethod paymentMethod,
            @Schema(description = "가상계좌 정보")
            AccountInfo virtualAccount,
            @Schema(description = "결제 이력")
            PaymentHistoryItem paymentHistory,
            @Schema(description = "환불 이력")
            RefundHistoryItem refundHistory,
            @Schema(description = "환불 가능 여부", example = "true")
            boolean refundable
    ) {}

    public record ReceptionInfo(
            @Schema(description = "접수번호", example = "AS-20240101-001")
            String asReceptionNumber,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "제품명", example = "갤럭시 S24")
            String productName,
            @Schema(description = "증상", example = "액정 파손")
            String symptom,
            @Schema(description = "증상 상세", example = "화면 좌측 하단 깨짐")
            String symptomDetail
    ) {}

    public record AccountInfo(
            @Schema(description = "은행 코드", example = "88")
            String bankCode,
            @Schema(description = "계좌번호", example = "56201234567890")
            String accountNumber,
            @Schema(description = "입금 기한", example = "2024-01-02T23:59:59")
            LocalDateTime dueAt
    ) {}

    public record PaymentHistoryItem(
            @Schema(description = "결제 시도 ID", example = "1")
            Long attemptId,
            @Schema(description = "주문번호", example = "ORDER-20240101-001")
            String orderId,
            @Schema(description = "결제 금액", example = "50000")
            Long amount,
            @Schema(description = "결제 수단", example = "CARD")
            PaymentMethod paymentMethod,
            @Schema(description = "승인 시각", example = "2024-01-01T12:00:00")
            LocalDateTime approvedAt
    ) {
        public static PaymentHistoryItem from(PaymentAttempt attempt) {
            return new PaymentHistoryItem(attempt.getId(), attempt.getOrderId(),
                                          attempt.getAmount(), attempt.getMethod(), attempt.getApprovedAt());
        }
    }

    public record RefundHistoryItem(
            @Schema(description = "환불 ID", example = "1")
            Long refundId,
            @Schema(description = "환불 금액", example = "50000")
            Long amount,
            @Schema(description = "환불 사유", example = "고객 요청")
            String cancelReason,
            @Schema(description = "환불 상태", example = "DONE")
            PaymentRefundStatus status,
            @Schema(description = "환불 시각", example = "2024-01-03T15:00:00")
            LocalDateTime canceledAt
    ) {
        public static RefundHistoryItem from(PaymentRefund refund) {
            return new RefundHistoryItem(refund.getId(), refund.getCancelAmount(),
                                         refund.getCancelReason(), refund.getStatus(), refund.getCanceledAt());
        }
    }

    public record PaymentListResponse(
            @Schema(description = "청구서 ID", example = "1")
            Long billId,
            @Schema(description = "접수번호", example = "AS-20240101-001")
            String asReceptionNumber,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "청구 금액", example = "50000")
            Long amount,
            @Schema(description = "청구서 상태", example = "PAID")
            PaymentBillStatus status,
            @Schema(description = "결제 수단", example = "CARD")
            PaymentMethod paymentMethod

    ) {}


}
