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
public class AsReceptionTest {

    @Autowired
    private EntityManager em;

    private Long newAsReceptionId;
    private Customer customer;
    private Product product;
    private Member member;
    private Member technician;

    @BeforeEach
        // 각 테스트 케이스 실행 전에 실행 코드
    void setUp() {
        // 1. 접수 등록 사전 작업
        this.customer = new Customer("한소희", "010-5555-1212", "user@gamil.com", "12345", "00아파트 12동", "서울 강남구 양재동", 1L);
        this.product = new Product(1L, "SN-1004", LocalDateTime.now(), 1L);
        this.member = new Member(
                "user01@abc.com",
                "@#$@%",
                "최미소",
                "010-4008-4008",
                "서울시 강남구",
                Role.ROLE_RECEPTIONIST,
                "asdfas"
        );
        this.technician = new Member(
                "tech01@abc.com",
                "@#$@%",
                "김기사",
                "010-4009-4009",
                "서울시 서초구",
                Role.ROLE_TECHNICIAN,
                "technician01"
        );

        em.persist(this.customer);
        em.persist(this.product);
        em.persist(this.member);
        em.persist(technician);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("첨부파일 없이 접수 등록")
    void registration() {
        // given
        AsReception asReception = new AsReception(
                "AS-2026-1030",
                customer,
                product,
                member,
                technician,
                "충전불량",
                "충전이 안 됩니다",
                "일주일 내로 수리 부탁드립니다.",
                ReturnMethod.DELIVERY,
                null
        );

        // when
        em.persist(asReception);
        em.flush();

        Long receptionId = asReception.getId();
        Long technicianId = technician.getId();

        // DB에서 다시 조회하도록 영속성 컨텍스트 초기화
        em.clear();

        AsReception findReception = em.find(
                AsReception.class,
                receptionId
        );

        // then
        assertThat(findReception).isNotNull();
        assertThat(findReception.getId()).isEqualTo(receptionId);
        assertThat(findReception.getReceptionNumber())
                .isEqualTo("AS-2026-1030");
        assertThat(findReception.getSymptom()).isEqualTo("충전불량");
        assertThat(findReception.getSymptomDetail())
                .isEqualTo("충전이 안 됩니다");
        assertThat(findReception.getMemo())
                .isEqualTo("일주일 내로 수리 부탁드립니다.");
        assertThat(findReception.getTechnician().getId())
                .isEqualTo(technicianId);
        assertThat(findReception.getReturnMethod())
                .isEqualTo(ReturnMethod.DELIVERY);
        assertThat(findReception.getStatus())
                .isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(findReception.getStatusChangedAt()).isNotNull();
        assertThat(findReception.getFileUrl()).isNull();
    }

    @Test
    @DisplayName("파일 URL과 함께 접수 등록")
    void registrationWithFileUrl() {
        // given
        String fileUrl = "https://example.com/as/photo.jpg";
        AsReception asReception = new AsReception(
                "AS-2026-1031",
                customer,
                product,
                member,
                technician,
                "충전불량",
                "충전이 안 됩니다",
                "일주일 내로 수리 부탁드립니다.",
                ReturnMethod.DELIVERY,
                fileUrl
        );

        // when
        em.persist(asReception);
        em.flush();

        Long receptionId = asReception.getId();
        em.clear();

        AsReception findReception = em.find(AsReception.class, receptionId);

        // then
        assertThat(findReception.getFileUrl()).isEqualTo(fileUrl);
    }

    @Test
    @DisplayName("담당 기사를 다른 기사로 바꾼다")
    void updateTechnician() {
        Member newTechnician = new Member(
                "tech02@abc.com",
                "@#$@%",
                "박기사",
                "010-0000-0001",
                "서울시 강남구",
                Role.ROLE_TECHNICIAN,
                "technician02"
        );
        em.persist(newTechnician);

        AsReception asReception = new AsReception(
                "AS-2026-1032",
                customer,
                product,
                member,
                technician,
                "충전불량",
                "충전이 안 됩니다",
                "메모",
                ReturnMethod.DELIVERY,
                null
        );
        em.persist(asReception);
        em.flush();

        asReception.updateTechnician(newTechnician);
        em.flush();
        em.clear();

        AsReception found = em.find(AsReception.class, asReception.getId());
        assertThat(found.getTechnician().getId()).isEqualTo(newTechnician.getId());
        assertThat(found.getTechnician().getName()).isEqualTo("박기사");
    }

}
