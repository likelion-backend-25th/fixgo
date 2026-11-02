package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaConfig.class)
@DataJpaTest
public class AsRequestTest {

    @Autowired
    private EntityManager em;

    private Long newAsRequestId;
    private Customer customer;
    private Product product;
    private Member member;
    private Member technician;

    @BeforeEach
        // 각 테스트 케이스 실행 전에 실행 코드
    void setUp() {
        // 1. 접수 등록 사전 작업
        this.customer = new Customer("한소희", "010-5555-1212", "user@gamil.com", "12345", "00아파트 12동", "서울 강남구 양재동", "111");
        this.product = new Product(1L, "SN-1004", LocalDateTime.now(), "admin");
        this.member = new Member("4008", "user01@abc.com", "@#$@%", "최미소", Role.ROLE_RECEPTIONIST, "asdfas");
        this.technician = new Member("4009", "tech01@abc.com", "@#$@%", "김기사", Role.ROLE_TECHNICIAN, "technician01");

        em.persist(this.customer);
        em.persist(this.product);
        em.persist(this.member);
        em.persist(technician);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("접수 등록")
    void registration() {

        AsRequest asRequest = new AsRequest("AS-2026-1030", customer, product, member, technician, "충전불량", "충전이 안 됩니다", "일주일 내로 수리 부탁드립니다.", ReturnMethod.DELIVERY);

        em.persist(asRequest); // insert 쿼리문이 실행되어 접수가 등록됨
        em.flush();

        Long requestId = asRequest.getId();
        Long technicianId = technician.getId();

        // DB에서 다시 조회하도록 영속성 컨텍스트 초기화
        em.clear();

        AsRequest findRequest = em.find(AsRequest.class, asRequest.getId());

        assertThat(findRequest).isNotNull();
        assertThat(findRequest.getId()).isEqualTo(asRequest.getId());
        assertThat(findRequest.getSymptom()).isEqualTo(asRequest.getSymptom());
        assertThat(findRequest.getTechnician().getId()).isEqualTo(technicianId);

    }

}
