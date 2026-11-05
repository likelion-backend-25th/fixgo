package net.likelion.bebc25.fixgo.payment.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.payment.dto.PaymentDto;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBill;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.repository.PaymentAttemptRepository;
import net.likelion.bebc25.fixgo.payment.repository.PaymentBillRepository;
import net.likelion.bebc25.fixgo.support.AsReceptionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaymentAttemptService 동시성 테스트.
 * <p>
 * 여러 스레드가 각자 트랜잭션을 커밋해야 잠금이 검증되므로 테스트 트랜잭션을 끈다(NOT_SUPPORTED).
 * 그래서 데이터가 롤백되지 않으며, 테스트마다 직접 지운다.
 */
@DataJpaTest
@Import({JpaConfig.class, AsReceptionFixture.class, PaymentAttemptService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentAttemptServiceConcurrencyTest {
    private static final int THREAD_COUNT = 8;

    @Autowired
    PaymentAttemptService paymentAttemptService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    PaymentBillRepository paymentBillRepository;
    @Autowired
    AsReceptionFixture asReceptionFixture;

    @AfterEach
    void cleanUp() {
        paymentAttemptRepository.deleteAll();
        paymentBillRepository.deleteAll();
        asReceptionFixture.deleteAll();
    }

    @Test
    @DisplayName("같은 청구서에 동시에 결제를 시작해도 결제 시도는 하나만 만들어지고 모두 같은 orderId를 받는다.")
    void startPayment_concurrent() throws Exception {
        // given
        AsReception reception = asReceptionFixture.save();
        PaymentBill bill = paymentBillRepository.save(PaymentBill.create(reception, 30_000L, PaymentBillType.REPAIR));
        Long asReceptionId = reception.getId();

        // when: 모든 스레드가 준비된 뒤 한꺼번에 startPayment를 호출한다
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<PaymentDto.StartPaymentResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                Callable<PaymentDto.StartPaymentResponse> task = () -> {
                    ready.countDown();
                    start.await();
                    return paymentAttemptService.startPayment(asReceptionId);
                };
                futures.add(executor.submit(task));
            }
            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            // then: 모든 요청이 성공하고 같은 attempt를 받는다 (get()은 스레드 예외를 다시 던진다)
            List<String> orderIds = new ArrayList<>();
            for (Future<PaymentDto.StartPaymentResponse> future : futures) {
                orderIds.add(future.get(10, TimeUnit.SECONDS).orderId());
            }
            assertThat(orderIds).containsOnly(orderIds.getFirst());
        } finally {
            executor.shutdownNow();
        }

        assertThat(paymentAttemptRepository.findAllByPaymentBillId(bill.getId()))
                .singleElement()
                .satisfies(a -> assertThat(a.getStatus()).isEqualTo(PaymentAttemptStatus.READY));
    }
}
