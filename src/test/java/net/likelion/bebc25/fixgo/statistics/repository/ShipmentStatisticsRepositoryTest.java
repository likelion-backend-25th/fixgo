package net.likelion.bebc25.fixgo.statistics.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.shipment.entity.Shipment;
import net.likelion.bebc25.fixgo.shipment.entity.ShipmentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ShipmentStatisticsRepositoryTest {

    private static final String PREFIX = "SHIPMENT-STAT-";

    @Autowired
    ShipmentStatisticsRepository shipmentStatisticsRepository;

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
            entityManager.createQuery("DELETE FROM Shipment").executeUpdate();
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
    @DisplayName("배송대기, 배송 중, 완료 건수를 집계한다.")
    void countByStatus() {
        transactionTemplate.executeWithoutResult(status -> {
            Member member = new Member(PREFIX + "admin@test.com", "password", "접수 담당자", "010-1111-1111", "서울시 강남구",
                                       Role.ROLE_ADMIN, "");
            Customer customer = new Customer("테스트 고객", "010-1234-5678", PREFIX + "customer@test.com", "12345", "서울시",
                                             "테스트 주소", 1L);
            Product product = new Product(1L, PREFIX + "SERIAL", LocalDateTime.now(), 1L);
            entityManager.persist(member);
            entityManager.persist(customer);
            entityManager.persist(product);

            persistShipment(customer, product, member, "1", ShipmentStatus.READY);
            persistShipment(customer, product, member, "2", ShipmentStatus.READY);
            persistShipment(customer, product, member, "3", ShipmentStatus.SHIPPING);
            persistShipment(customer, product, member, "4", ShipmentStatus.DELIVERED);
            persistShipment(customer, product, member, "5", ShipmentStatus.PICKED_UP);
            persistShipment(customer, product, member, "6", ShipmentStatus.READY, LocalDateTime.now().minusYears(1));
        });

        LocalDateTime startDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime endDateTime = LocalDate.now().plusDays(1).atStartOfDay();

        ShipmentStatisticsRepository.ShipmentStatistics statistics = shipmentStatisticsRepository.countByStatus(
                ShipmentStatus.READY, ShipmentStatus.SHIPPING, ShipmentStatus.DELIVERED, ShipmentStatus.PICKED_UP,
                startDateTime, endDateTime);

        assertThat(statistics.getReadyCount()).isEqualTo(2L);
        assertThat(statistics.getShippingCount()).isEqualTo(1L);
        assertThat(statistics.getCompletedCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("배송 건이 없으면 세 건수 모두 0이다.")
    void countByStatusWhenEmpty() {
        ShipmentStatisticsRepository.ShipmentStatistics statistics = shipmentStatisticsRepository.countByStatus(
                ShipmentStatus.READY, ShipmentStatus.SHIPPING, ShipmentStatus.DELIVERED, ShipmentStatus.PICKED_UP,
                LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

        assertThat(statistics.getReadyCount()).isZero();
        assertThat(statistics.getShippingCount()).isZero();
        assertThat(statistics.getCompletedCount()).isZero();
    }

    private void persistShipment(
            Customer customer, Product product, Member member, String number, ShipmentStatus shipmentStatus) {
        persistShipment(customer, product, member, number, shipmentStatus, LocalDateTime.now());
    }

    private void persistShipment(
            Customer customer, Product product, Member member, String number, ShipmentStatus shipmentStatus,
            LocalDateTime createdAt
    ) {
        AsReception reception = new AsReception(PREFIX + "RECEPTION-" + number, customer, product, member, member,
                                                "배터리", "배터리 상세", null, ReturnMethod.DELIVERY, null);
        entityManager.persist(reception);

        Shipment shipment = BeanUtils.instantiateClass(Shipment.class);
        ReflectionTestUtils.setField(shipment, "asReception", reception);
        ReflectionTestUtils.setField(shipment, "returnMethod", reception.getReturnMethod());
        ReflectionTestUtils.setField(shipment, "recipientName", "홍길동");
        ReflectionTestUtils.setField(shipment, "recipientPhone", "010-0000-0000");
        ReflectionTestUtils.setField(shipment, "status", shipmentStatus);
        ReflectionTestUtils.setField(shipment, "createdAt", createdAt);
        ReflectionTestUtils.setField(shipment, "updatedAt", createdAt);
        ReflectionTestUtils.setField(shipment, "createdBy", member);
        ReflectionTestUtils.setField(shipment, "updatedBy", member);
        entityManager.persist(shipment);
    }
}
