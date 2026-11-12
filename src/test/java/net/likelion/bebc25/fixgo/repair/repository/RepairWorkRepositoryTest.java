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
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
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
class RepairWorkRepositoryTest {

    @Autowired
    private RepairWorkRepository repairWorkRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AsReceptionRepository asRequestRepository;

    private Repair repair;

    @BeforeEach
    void setUp() {

        LocalDateTime now = LocalDateTime.now();

        // 담당 기사 및 접수자 생성
        Member technician = new Member(
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
        Customer customer = new Customer(
                "테스트 고객",
                "010-1111-2222",
                "customer@test.com",
                "12345",
                "대구광역시",
                "중구",
                1L
        );

        customer = customerRepository.save(customer);

        // 제품 생성
        Product product = new Product(
                1L,
                "TEST-SERIAL-001",
                now
        );

        product = productRepository.save(product);

        // A/S 접수 정보 생성
        AsReception asReception = new AsReception(
                "AS-TEST-001",
                customer,
                product,
                technician,
                technician,
                "메인보드 이상",
                "전원이 켜지지 않는 증상",
                "테스트 접수",
                ReturnMethod.DELIVERY,
                null
        );

        asReception = asRequestRepository.save(asReception);

        // 수리 정보 생성
        repair = new Repair(
                asReception,
                technician,
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        repair = repairRepository.save(repair);

        // 수리 작업 내역 생성
        RepairWork work1 = new RepairWork(
                repair,
                "부품 교체",
                "메인보드 교체"
        );

        RepairWork work2 = new RepairWork(
                repair,
                "부품 교체",
                "메모리 교체"
        );

        RepairWork work3 = new RepairWork(
                repair,
                "점검 작업",
                "메인보드 점검"
        );

        repairWorkRepository.saveAll(List.of(work1, work2, work3));
    }

    @Test
    @DisplayName("A/S 수리 건별 작업 내역 조회")
    void findByRepairId() {

        List<RepairWork> result =
                repairWorkRepository.findByRepair_Id(repair.getId());

        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(RepairWork::getWorkName)
                .containsExactlyInAnyOrder(
                        "메인보드 교체",
                        "메모리 교체",
                        "메인보드 점검"
                );
    }

    @Test
    @DisplayName("작업명 일부로 검색")
    void findByWorkName() {

        List<RepairWork> result =
                repairWorkRepository.findByWorkNameContaining("메인보드");

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(RepairWork::getWorkName)
                .containsExactlyInAnyOrder(
                        "메인보드 교체",
                        "메인보드 점검"
                );
    }

    @Test
    @DisplayName("존재하지 않는 작업명 검색")
    void findByWorkNameNotFound() {

        List<RepairWork> result =
                repairWorkRepository.findByWorkNameContaining("그래픽카드");

        assertThat(result).isEmpty();
    }
}