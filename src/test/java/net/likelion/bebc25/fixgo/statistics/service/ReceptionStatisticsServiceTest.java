package net.likelion.bebc25.fixgo.statistics.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.statistics.dto.ReceptionStatisticsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ReceptionStatisticsService.class, JpaConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReceptionStatisticsServiceTest {

    private static final String PREFIX = "RECEPTION-SERVICE-STAT-";

    @Autowired
    ReceptionStatisticsService receptionStatisticsService;

    @Autowired
    EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM AsReception ar WHERE ar.receptionNumber LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();
            entityManager.createQuery("DELETE FROM Product p WHERE p.serialNumber LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();
            entityManager.createQuery("DELETE FROM Customer c WHERE c.email LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();
            entityManager.createQuery("DELETE FROM Member m WHERE m.email LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();
        });
    }

    @Test
    @DisplayName("기간별 접수 건수, 완료 건수, 완료율을 조회한다.")
    void getReceptionStatistics() {
        transactionTemplate.executeWithoutResult(status -> {
            Member member = new Member(PREFIX + "admin@test.com", "password", "접수 담당자", "010-1111-1111", "서울시 강남구",
                                       Role.ROLE_ADMIN, "");
            Customer customer = new Customer("테스트 고객", "010-1234-5678", PREFIX + "customer@test.com", "12345", "서울시",
                                             "테스트 주소", 1L);
            Product product = new Product(1L, PREFIX + "SERIAL", LocalDateTime.now(), 1L);
            entityManager.persist(member);
            entityManager.persist(customer);
            entityManager.persist(product);

            persistReception(customer, product, member, "1", AsReceptionStatus.RECEIVED,
                             LocalDateTime.of(2026, 10, 5, 9, 0));
            persistReception(customer, product, member, "2", AsReceptionStatus.COMPLETED,
                             LocalDateTime.of(2026, 10, 20, 9, 0));
            persistReception(customer, product, member, "3", AsReceptionStatus.COMPLETED,
                             LocalDateTime.of(2026, 10, 31, 23, 59));
            persistReception(customer, product, member, "4", AsReceptionStatus.COMPLETED,
                             LocalDateTime.of(2026, 9, 30, 23, 59));
            persistReception(customer, product, member, "5", AsReceptionStatus.RECEIVED,
                             LocalDateTime.of(2026, 11, 1, 0, 0));
        });

        ReceptionStatisticsDto statistics = receptionStatisticsService.getReceptionStatistics(LocalDate.of(2026, 10, 1),
                                                                                              LocalDate.of(2026, 10,
                                                                                                           31));

        assertThat(statistics.startDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(statistics.endDate()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(statistics.receivedCount()).isEqualTo(3L);
        assertThat(statistics.completedCount()).isEqualTo(2L);
        assertThat(statistics.completionRate()).isEqualByComparingTo(new BigDecimal("66.7"));
    }

    @Test
    @DisplayName("기간 안 접수가 없으면 건수는 0이고 완료율도 0이다.")
    void getReceptionStatisticsWhenEmpty() {
        ReceptionStatisticsDto statistics = receptionStatisticsService.getReceptionStatistics(LocalDate.of(2026, 10, 1),
                                                                                              LocalDate.of(2026, 10,
                                                                                                           31));

        assertThat(statistics.receivedCount()).isZero();
        assertThat(statistics.completedCount()).isZero();
        assertThat(statistics.completionRate()).isEqualByComparingTo(new BigDecimal("0.0"));
    }

    @Test
    @DisplayName("조회 시작일이 null이면 예외가 발생한다.")
    void nullStartDate() {
        assertThatThrownBy(
                () -> receptionStatisticsService.getReceptionStatistics(null, LocalDate.of(2026, 10, 31))).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("조회 종료일이 null이면 예외가 발생한다.")
    void nullEndDate() {
        assertThatThrownBy(
                () -> receptionStatisticsService.getReceptionStatistics(LocalDate.of(2026, 10, 1), null)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다.")
    void invalidDateRange() {
        assertThatThrownBy(() -> receptionStatisticsService.getReceptionStatistics(LocalDate.of(2026, 11, 1),
                                                                                   LocalDate.of(2026, 10,
                                                                                                31))).isInstanceOf(
                IllegalArgumentException.class);
    }

    private void persistReception(
            Customer customer, Product product, Member member, String number, AsReceptionStatus receptionStatus,
            LocalDateTime createdAt
    ) {
        AsReception reception = new AsReception(PREFIX + "RECEPTION-" + number, customer, product, member, member,
                                                "배터리", "배터리 상세", null, ReturnMethod.DELIVERY, null);
        if (receptionStatus == AsReceptionStatus.COMPLETED) {
            reception.changeStatus(AsReceptionStatus.COMPLETED);
        }
        entityManager.persist(reception);
        entityManager.flush();

        entityManager.createNativeQuery("""
                                                UPDATE as_reception
                                                SET created_at = :createdAt
                                                WHERE reception_number = :receptionNumber
                                                """).setParameter("createdAt", createdAt)
                     .setParameter("receptionNumber", PREFIX + "RECEPTION-" + number).executeUpdate();
        entityManager.clear();
    }
}