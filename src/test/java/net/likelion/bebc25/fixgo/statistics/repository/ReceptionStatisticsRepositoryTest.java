package net.likelion.bebc25.fixgo.statistics.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReceptionStatisticsRepositoryTest {

    private static final String PREFIX = "RECEPTION-STAT-";

    @Autowired
    ReceptionStatisticsRepository receptionStatisticsRepository;

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
    @DisplayName("기간 내 접수 건수와 그중 완료 건수를 집계한다.")
    void countByPeriod() {
        transactionTemplate.executeWithoutResult(status -> {
            Member member = new Member(PREFIX + "admin@test.com", "password", "접수 담당자", "010-1111-1111", "서울시 강남구",
                                       Role.ROLE_ADMIN, "");
            Customer customer = new Customer("테스트 고객", "010-1234-5678", PREFIX + "customer@test.com", "12345", "서울시",
                                             "테스트 주소", 1L);
            Product product = new Product(1L, PREFIX + "SERIAL", LocalDateTime.now());
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

        ReceptionStatisticsRepository.ReceptionStatistics statistics = receptionStatisticsRepository.countByPeriod(
                AsReceptionStatus.COMPLETED, LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 11, 1, 0, 0));

        assertThat(statistics.getReceivedCount()).isEqualTo(3L);
        assertThat(statistics.getCompletedCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("기간 안 접수가 없으면 두 건수 모두 0이다.")
    void countByPeriodWhenEmpty() {
        ReceptionStatisticsRepository.ReceptionStatistics statistics = receptionStatisticsRepository.countByPeriod(
                AsReceptionStatus.COMPLETED, LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 11, 1, 0, 0));

        assertThat(statistics.getReceivedCount()).isZero();
        assertThat(statistics.getCompletedCount()).isZero();
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