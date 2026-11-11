package net.likelion.bebc25.fixgo.statistics.service;

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
import net.likelion.bebc25.fixgo.statistics.dto.LongPendingStatisticsDto;
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
@Import({LongPendingStatisticsService.class, JpaConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LongPendingStatisticsServiceTest {

    private static final String PREFIX = "LONG-PENDING-SERVICE-TEST-";

    @Autowired
    LongPendingStatisticsService longPendingStatisticsService;

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
    @DisplayName("14일 이상 상태가 변경되지 않은 접수 목록과 경과일을 조회한다.")
    void getLongPendingReceptions() {
        LocalDateTime now = LocalDateTime.now();

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

            Product product = new Product(model.getId(), PREFIX + "SERIAL", now.minusYears(1), 1L);

            entityManager.persist(product);
            entityManager.flush();

            persistReception(customer, product, member, "1", AsReceptionStatus.RECEIVED, now.minusDays(20));

            persistReception(customer, product, member, "2", AsReceptionStatus.INSPECTION, now.minusDays(14));

            persistReception(customer, product, member, "3", AsReceptionStatus.REPAIR_IN_PROGRESS, now.minusDays(10));

            persistReception(customer, product, member, "4", AsReceptionStatus.COMPLETED, now.minusDays(30));
        });

        List<LongPendingStatisticsDto.LongPendingResponse> result = longPendingStatisticsService.getLongPendingReceptions();

        assertThat(result).hasSize(2);

        assertThat(result).extracting(LongPendingStatisticsDto.LongPendingResponse::receptionNumber)
                          .containsExactly(PREFIX + "RECEPTION-1", PREFIX + "RECEPTION-2");

        assertThat(result.get(0).customerName()).isEqualTo("테스트 고객");
        assertThat(result.get(0).modelName()).isEqualTo("Galaxy Test");
        assertThat(result.get(0).status()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(result.get(0).technicianName()).isEqualTo("테스트 기사");
        assertThat(result.get(0).elapsedDays()).isGreaterThanOrEqualTo(20);
        assertThat(result.get(0).lastActivityAt()).isNotNull();
    }

    @Test
    @DisplayName("장기 미처리 접수가 없으면 빈 목록을 반환한다.")
    void getLongPendingReceptionsWhenEmpty() {
        List<LongPendingStatisticsDto.LongPendingResponse> result = longPendingStatisticsService.getLongPendingReceptions();

        assertThat(result).isEmpty();
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
