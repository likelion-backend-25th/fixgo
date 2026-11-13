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
import net.likelion.bebc25.fixgo.repair.entity.RepairSymptomPreset;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class RepairSymptomPresetRepositoryTest {

    @Autowired
    private RepairSymptomPresetRepository repairSymptomPresetRepository;

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

    @BeforeEach
    void setUp() {

        LocalDateTime now = LocalDateTime.now();

        // 테스트용 기사 생성
        Member technician = new Member(
                "technician@test.com",
                "testPassword",
                "테스트 기사",
                "010-1234-5678",
                "서울시 강남구",
                Role.ROLE_TECHNICIAN);

        technician = memberRepository.save(technician);

        // 테스트용 고객 생성
        Customer customer = new Customer("테스트 고객", "010-1111-2222", "customer@test.com", "12345", "대구광역시", "중구", 1L);

        customer = customerRepository.save(customer);

        // 테스트용 제품 생성
        Product product = new Product(1L, "TEST-SERIAL-001", now);

        product = productRepository.save(product);

        // 테스트용 A/S 접수 생성
        AsReception asReception = new AsReception("AS-TEST-001", customer, product, technician, technician, "메인보드 이상", "전원이 켜지지 않는 증상", "테스트 접수", ReturnMethod.DELIVERY, null);

        asReception = asReceptionRepository.save(asReception);

        // 테스트용 수리 데이터 생성
        repair = new Repair(asReception, technician, WarrantyResult.FREE, RepairStatus.REQUESTED);

        repair = repairRepository.save(repair);

        // 테스트용 증상 프리셋 데이터 생성
        RepairSymptomPreset preset1 = new RepairSymptomPreset(repair, "전원 불량");

        RepairSymptomPreset preset2 = new RepairSymptomPreset(repair, "화면 출력 불량");

        RepairSymptomPreset preset3 = new RepairSymptomPreset(repair, "전원 버튼 작동 불량");

        repairSymptomPresetRepository.saveAll(List.of(preset1, preset2, preset3));
    }

    @Test
    @DisplayName("A/S 수리 건별 증상 프리셋 조회")
    void findByRepairId() {

        List<RepairSymptomPreset> result = repairSymptomPresetRepository.findByRepairId(repair.getId());

        assertThat(result).hasSize(3);

        assertThat(result).extracting(RepairSymptomPreset::getSymptomPresetNam).containsExactlyInAnyOrder("전원 불량", "화면 출력 불량", "전원 버튼 작동 불량");
    }

    @Test
    @DisplayName("증상 프리셋 이름 일부로 검색")
    void findBySymptomPresetName() {

        List<RepairSymptomPreset> result = repairSymptomPresetRepository.findBySymptomPresetName("전원");

        assertThat(result).hasSize(2);

        assertThat(result).extracting(RepairSymptomPreset::getSymptomPresetNam).containsExactlyInAnyOrder("전원 불량", "전원 버튼 작동 불량");
    }

    @Test
    @DisplayName("존재하지 않는 증상 프리셋 검색")
    void findBySymptomPresetNameNotFound() {

        List<RepairSymptomPreset> result = repairSymptomPresetRepository.findBySymptomPresetName("그래픽카드");

        assertThat(result).isEmpty();
    }
}
