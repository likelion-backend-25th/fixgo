package net.likelion.bebc25.fixgo.statistics.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LongPendingStatisticsRepositoryTest {

    private static final String PREFIX = "LONG-PENDING-TEST-";

    @Autowired
    LongPendingStatisticsRepository repository;

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

            entityManager.createQuery("DELETE FROM ProductModel pm WHERE pm.name LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();

            entityManager.createQuery("DELETE FROM Customer c WHERE c.email LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();

            entityManager.createQuery("DELETE FROM Member m WHERE m.email LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();
        });
    }

    @Test
    @DisplayName("상태 변경 후 14일 이상 지난 미완료 접수만 조회한다.")
    void findLongPendingReceptions() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 6, 10, 0);
        LocalDateTime threshold = now.minusDays(14);

        transactionTemplate.executeWithoutResult(status -> {
            Member member = new Member(PREFIX + "admin@test.com", "password", "테스트 기사", "010-1111-1111", "서울시",
                                       Role.ROLE_ADMIN, "");

            Customer customer = new Customer("테스트 고객", "010-1234-5678", PREFIX + "customer@test.com", "12345", "서울시",
                                             "테스트 주소", 1L);

            ProductModel model = new ProductModel(PREFIX + "갤럭시", "Galaxy Test", Category.PHONE, 12, 1L);

            entityManager.persist(member);
            entityManager.persist(customer);
            entityManager.persist(model);
            entityManager.flush();

            Product product = new Product(model.getId(), PREFIX + "SERIAL", now.minusYears(1));

            entityManager.persist(product);
            entityManager.flush();

            persistReception(customer, product, member, "1", AsReceptionStatus.RECEIVED, threshold.minusDays(5));

            persistReception(customer, product, member, "2", AsReceptionStatus.INSPECTION, threshold);

            persistReception(customer, product, member, "3", AsReceptionStatus.REPAIR_IN_PROGRESS,
                             threshold.plusDays(1));

            persistReception(customer, product, member, "4", AsReceptionStatus.COMPLETED, threshold.minusDays(10));
        });

        List<LongPendingStatisticsRepository.LongPendingStatistics> result = repository.findLongPendingReceptions(
                AsReceptionStatus.COMPLETED, threshold);

        assertThat(result).hasSize(2);

        assertThat(result).extracting(LongPendingStatisticsRepository.LongPendingStatistics::getReceptionNumber)
                          .containsExactly(PREFIX + "RECEPTION-1", PREFIX + "RECEPTION-2");

        assertThat(result.get(0).getCustomerName()).isEqualTo("테스트 고객");
        assertThat(result.get(0).getModelName()).isEqualTo("Galaxy Test");
        assertThat(result.get(0).getStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(result.get(0).getTechnicianName()).isEqualTo("테스트 기사");
        assertThat(result.get(0).getStatusChangedAt()).isEqualTo(threshold.minusDays(5));
    }

    private void persistReception(
            Customer customer, Product product, Member member, String number, AsReceptionStatus receptionStatus,
            LocalDateTime statusChangedAt
    ) {
        AsReception reception = new AsReception(PREFIX + "RECEPTION-" + number, customer, product, member, member,
                                                "배터리", "배터리 상세", null, ReturnMethod.DELIVERY, null);

        if (receptionStatus != AsReceptionStatus.RECEIVED) {
            reception.changeStatus(receptionStatus);
        }

        entityManager.persist(reception);
        entityManager.flush();

        entityManager.createNativeQuery("""
                                                UPDATE as_reception
                                                SET status_changed_at = :statusChangedAt
                                                WHERE reception_number = :receptionNumber
                                                """).setParameter("statusChangedAt", statusChangedAt)
                     .setParameter("receptionNumber", PREFIX + "RECEPTION-" + number).executeUpdate();

        entityManager.clear();
    }
}
