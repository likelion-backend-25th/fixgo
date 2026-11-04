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
import net.likelion.bebc25.fixgo.repair.entity.RepairPhoto;
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
class RepairPhotoRepositoryTest {

    @Autowired
    private RepairPhotoRepository repairPhotoRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AsReceptionRepository AsReceptionRepository;

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
                Role.ROLE_TECHNICIAN,
                "default.png"
        );

        technician = memberRepository.save(technician);

        // 테스트용 고객 생성
        Customer customer = new Customer("테스트 고객", "010-1111-2222", "customer@test.com", "12345", "대구광역시", "중구", "tester");

        customer = customerRepository.save(customer);

        // 테스트용 제품 생성
        Product product = new Product(1L, "TEST-SERIAL-001", now, "tester");

        product = productRepository.save(product);

        // 테스트용 A/S 접수 생성
        AsReception asReception = new AsReception("AS-TEST-001", customer, product, technician, technician, "메인보드 이상", "전원이 켜지지 않는 증상", "테스트 접수", ReturnMethod.DELIVERY, null);

        asReception = AsReceptionRepository.save(asReception);

        // 테스트용 수리 데이터 생성
        repair = new Repair(asReception, technician, WarrantyResult.FREE, RepairStatus.REQUESTED);

        repair = repairRepository.save(repair);

        // 테스트용 수리 사진 데이터 생성
        RepairPhoto photo1 = new RepairPhoto(repair, "수리 전", "https://example.com/before1.jpg", now, now, "tester", "tester");

        RepairPhoto photo2 = new RepairPhoto(repair, "수리 중", "https://example.com/working1.jpg", now, now, "tester", "tester");

        RepairPhoto photo3 = new RepairPhoto(repair, "수리 후", "https://example.com/after1.jpg", now, now, "tester", "tester");

        repairPhotoRepository.saveAll(List.of(photo1, photo2, photo3));
    }

    @Test
    @DisplayName("A/S 수리 건별 사진 목록 조회")
    void findByRepairId() {

        List<RepairPhoto> result = repairPhotoRepository.findByRepairId(repair.getId());

        assertThat(result).hasSize(3);

        assertThat(result).extracting(RepairPhoto::getPhotoType).containsExactlyInAnyOrder("수리 전", "수리 중", "수리 후");
    }

    @Test
    @DisplayName("A/S 수리 건별 사진 유형 조회")
    void findByRepairIdAndPhotoType() {

        List<RepairPhoto> result = repairPhotoRepository.findByRepairIdAndPhotoType(repair.getId(), "수리 전");

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getPhotoType()).isEqualTo("수리 전");

        assertThat(result.get(0).getFileUrl()).isEqualTo("https://example.com/before1.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 사진 유형 조회")
    void findByRepairIdAndPhotoTypeNotFound() {

        List<RepairPhoto> result = repairPhotoRepository.findByRepairIdAndPhotoType(repair.getId(), "기타");

        assertThat(result).isEmpty();
    }
}
