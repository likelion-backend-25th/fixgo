package net.likelion.bebc25.fixgo.payment.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PaymentBillTest {
    @Test
    @DisplayName("청구서를 발행하면 UNPAID 상태이고 paidAt은 비어있다.")
    void issue_success() {
        // given & when
        PaymentBill bill = PaymentBill.issue(1L, 30000L, PaymentBillType.REPAIR);

        // then
        assertThat(bill.getStatus()).isEqualTo(PaymentBillStatus.UNPAID);
        assertThat(bill.getAmount()).isEqualTo(30000L);
        assertThat(bill.getPaidAt()).isNull();
    }

    @Test
    @DisplayName("결제 완료 처리하면 PAID가 되고 paidAt이 채워진다.")
    void markPaid_success() {
        // given
        PaymentBill bill = PaymentBill.issue(1L, 30000L, PaymentBillType.REPAIR);

        // when
        bill.markPaid();

        // then
        assertThat(bill.getStatus()).isEqualTo(PaymentBillStatus.PAID);
        assertThat(bill.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 PAID인 청구서를 다시 결제 완료 처리하면 예외가 난다")
    void markPaid_twice() {
        PaymentBill bill = PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR);
        bill.markPaid();

        assertThatThrownBy(bill::markPaid).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("asRequestId가 null이면 발행할 수 없다")
    void issue_nullAsRequestId() {
        assertThatThrownBy(() -> PaymentBill.issue(null, 30_000L, PaymentBillType.REPAIR)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A/S 접수 ID");
    }

    @Test
    @DisplayName("amount가 null이면 발행할 수 없다")
    void issue_nullAmount() {
        assertThatThrownBy(() -> PaymentBill.issue(1L, null, PaymentBillType.REPAIR)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("전이에 실패한 markPaid()는 paidAt을 바꾸지 않는다")
    void markPaid_failure_keepsPaidAt() {
        PaymentBill bill = PaymentBill.issue(1L, 30_000L, PaymentBillType.REPAIR);
        bill.markPaid();
        LocalDateTime firstPaidAt = bill.getPaidAt();

        assertThatThrownBy(bill::markPaid).isInstanceOf(IllegalStateException.class);

        assertThat(bill.getPaidAt()).isEqualTo(firstPaidAt);
    }
}
