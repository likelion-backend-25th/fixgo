package net.likelion.bebc25.fixgo.payment.dto;

import net.likelion.bebc25.fixgo.payment.entity.PaymentAttempt;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentMethod;
import net.likelion.bebc25.fixgo.payment.entity.VirtualAccount;

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
            String orderId,
            String paymentKey,
            Long amount
    ) {}

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

        public enum Status {NOT_ISSUED, UNPAID, WAITING_FOR_DEPOSIT, PAID}

        /**
         * 가상계좌 입금 안내 정보. 고객이 입금할 때 필요한 값만 담는다.
         */
        public record VirtualAccountInfo(
                String bankName,
                String accountNumber,
                LocalDateTime dueAt
        ) {}


    }
}
