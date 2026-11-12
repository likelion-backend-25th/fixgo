package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class RepairRepositoryTest {

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AsReceptionRepository asReceptionRepository;

    private Repair repair;
    private Member technician;

    @BeforeEach
    void setUp() {

        LocalDateTime now = LocalDateTime.now();

        // 담당 기사 생성
        technician = new Member(
                "technician@test.com",
                "testPassword",
                "테스트 기사",
                "010-1234-5678",
                "서울시 강남구",
                Role.ROLE_TECHNICIAN,
                "default.png"
        );

        technician = memberRepository.save(technician);

        // 고객 생성
        Customer customer = new Customer("테스트 고객", "010-1111-2222", "customer@test.com", "12345", "대구광역시", "중구", 1L);

        customer = customerRepository.save(customer);

        // 제품 생성
        Product product = new Product(1L, "TEST-SERIAL-001", now);

        product = productRepository.save(product);

        // A/S 접수 생성
        AsReception asReception = new AsReception("AS-TEST-001", customer, product, technician, technician, "메인보드 이상", "전원이 켜지지 않는 증상", "테스트 접수", ReturnMethod.DELIVERY, null);

        asReception = asReceptionRepository.save(asReception);

        // 수리 생성
        repair = new Repair(asReception, technician, WarrantyResult.FREE, RepairStatus.REQUESTED);

        repair = repairRepository.save(repair);
    }

    @Test
    @DisplayName("A/S 접수 ID로 수리 정보 조회")
    void findByAsReceptionIdTest() {

        Optional<Repair> result = repairRepository.findByAsReception_Id(repair.getAsReception().getId());

        assertThat(result).isPresent();
        assertThat(result.get().getAsReception().getId()).isEqualTo(repair.getAsReception().getId());
    }

    @Test
    @DisplayName("담당 기사별 수리 목록 조회")
    void findByTechnicianIdTest() {

        List<Repair> result = repairRepository.findByAsReception_Technician_Id(technician.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnician().getId()).isEqualTo(technician.getId());
    }

    @Test
    @DisplayName("수리에 이전 기사가 남아 있어도 현재 접수 담당자에게 표시한다")
    void findsRepairByCurrentReceptionTechnician() {
        Member newTechnician = memberRepository.save(new Member(
                "new-technician@test.com", "testPassword", "새 기사", "010-5555-7777",
                "서울시", Role.ROLE_TECHNICIAN, "default.png"));
        repair.getAsReception().updateTechnician(newTechnician);

        assertThat(RepairResponse.from(repair).technicianId()).isEqualTo(newTechnician.getId());
        assertThat(repairRepository.findByAsReception_Technician_Id(technician.getId())).isEmpty();
        assertThat(repairRepository.findByAsReception_Technician_Id(newTechnician.getId()))
                .extracting(Repair::getId).containsExactly(repair.getId());
        assertThat(repairRepository.findByIdAndAsReception_Technician_Id(repair.getId(), technician.getId()))
                .isEmpty();
        assertThat(repairRepository.findByIdAndAsReception_Technician_Id(repair.getId(), newTechnician.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("수리 상태별 목록 조회")
    void findByStatusTest() {

        List<Repair> result = repairRepository.findByStatus(RepairStatus.REQUESTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RepairStatus.REQUESTED);
    }

    @Test
    @DisplayName("담당 기사 및 수리 상태별 목록 조회")
    void findByTechnicianIdAndStatusTest() {

        List<Repair> result = repairRepository.findByTechnician_IdAndStatus(technician.getId(), RepairStatus.REQUESTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnician().getId()).isEqualTo(technician.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(RepairStatus.REQUESTED);
    }
}
