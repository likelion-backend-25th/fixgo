package net.likelion.bebc25.fixgo.as.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.repository.MemberRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class AsReceptionRepositoryTest {

    @Autowired
    private AsReceptionRepository asReceptionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductModelRepository productModelRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private EntityManager em;

    private Member kim;
    private Member park;
    private AsReception alpha;
    private AsReception beta;
    private Long alphaCustomerId;

    private static List<String> numbersOf(Page<AsReceptionDto.ListResponse> page) {
        return page.getContent().stream().map(AsReceptionDto.ListResponse::receptionNumber).toList();
    }

    private static AsReceptionDto.ListResponse findByNumber(
            Page<AsReceptionDto.ListResponse> page,
            String receptionNumber
    ) {
        return page.getContent().stream()
                .filter(response -> response.receptionNumber().equals(receptionNumber))
                .findFirst()
                .orElseThrow();
    }

    @BeforeEach
    void setUp() {
        Member receptionist = memberRepository.save(
                new Member(
                        "receptionist@test.com",
                        "password",
                        "최접수",
                        "010-0000-0001",
                        "서울시 강남구",
                        Role.ROLE_RECEPTIONIST)
        );
        kim = memberRepository.save(
                new Member(
                        "kim@test.com",
                        "password",
                        "김기사",
                        "010-0000-0002",
                        "서울시 강남구",
                        Role.ROLE_TECHNICIAN)
        );
        park = memberRepository.save(
                new Member(
                        "park@test.com",
                        "password",
                        "박기사",
                        "010-0000-0003",
                        "서울시 강남구",
                        Role.ROLE_TECHNICIAN)
        );
        Long createdBy = receptionist.getId();

        ProductModel model = productModelRepository.save(
                new ProductModel("그램", "Gram 15", Category.LAPTOP, 12, createdBy)
        );
        Customer hansohee = customerRepository.save(
                new Customer("한소희", "010-1111-2222", "hansohee@test.com", "12345", "서울", "강남구", createdBy)
        );
        Customer kimcheolsu = customerRepository.save(
                new Customer("김철수", "010-3333-4444", "kimcheolsu@test.com", "54321", "대구", "중구", createdBy)
        );
        Product gram = productRepository.save(new Product(model.getId(), "SN-ALPHA", LocalDateTime.now()));
        Product phone = productRepository.save(new Product(model.getId(), "SN-BETA", LocalDateTime.now()));

        alphaCustomerId = hansohee.getId();
        alpha = saveReception("AS-ALPHA-001", hansohee, gram, receptionist, kim, "LCD불량");
        beta = saveReception("AS-BETA-002", kimcheolsu, phone, receptionist, park, "충전불가");
        repairRepository.save(new Repair(alpha, kim, WarrantyResult.FREE, RepairStatus.DIAGNOSING));

        em.flush();
        em.createQuery("update AsReception r set r.status = :status where r.id = :id")
                .setParameter("status", AsReceptionStatus.INSPECTION)
                .setParameter("id", beta.getId())
                .executeUpdate();
        em.clear();
    }

    @Test
    @DisplayName("조건이 없으면 접수 목록을 ListResponse 필드로 반환한다")
    void search_withoutFilters() {
        Page<AsReceptionDto.ListResponse> result = asReceptionRepository.search(
                null,
                null,
                null,
                PageRequest.of(0,
                10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(AsReceptionDto.ListResponse::receptionNumber)
                .containsExactlyInAnyOrder("AS-ALPHA-001", "AS-BETA-002");

        AsReceptionDto.ListResponse alphaResponse = findByNumber(result, "AS-ALPHA-001");
        assertThat(alphaResponse.id()).isEqualTo(alpha.getId());
        assertThat(alphaResponse.createdAt()).isNotNull();
        assertThat(alphaResponse.customerName()).isEqualTo("한소희");
        assertThat(alphaResponse.customerPhone()).isEqualTo("010-1111-2222");
        assertThat(alphaResponse.modelName()).isEqualTo("Gram 15");
        assertThat(alphaResponse.receptionStatus()).isEqualTo(AsReceptionStatus.RECEIVED);
        assertThat(alphaResponse.repairStatus()).isEqualTo(RepairStatus.DIAGNOSING);
        assertThat(findByNumber(result, "AS-BETA-002").repairStatus()).isNull();
        assertThat(alphaResponse.technicianName()).isEqualTo("김기사");
    }

    @Test
    @DisplayName("접수번호, 고객명, 증상으로 검색하고 대소문자는 구분하지 않는다")
    void search_keyword() {
        assertThat(numbersOf(asReceptionRepository.search("as-alpha", null, null, PageRequest.of(0, 10))))
                .containsExactly("AS-ALPHA-001");
        assertThat(numbersOf(asReceptionRepository.search("철수", null, null, PageRequest.of(0, 10))))
                .containsExactly("AS-BETA-002");
        assertThat(numbersOf(asReceptionRepository.search("lcd", null, null, PageRequest.of(0, 10))))
                .containsExactly("AS-ALPHA-001");
        assertThat(asReceptionRepository.search("없는검색어", null, null, PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    @Test
    @DisplayName("상태와 담당 기사로 목록을 거른다")
    void search_statusAndTechnician() {
        assertThat(numbersOf(asReceptionRepository.search(
                    null,
                    AsReceptionStatus.RECEIVED,
                    null,
                    PageRequest.of(0,
                    10
                ))))
                .containsExactly("AS-ALPHA-001");
        assertThat(numbersOf(asReceptionRepository.search(
                    null,
                    AsReceptionStatus.INSPECTION,
                    null,
                    PageRequest.of(0,
                    10
                ))))
                .containsExactly("AS-BETA-002");
        assertThat(numbersOf(asReceptionRepository.search(null, null, kim.getId(), PageRequest.of(0, 10))))
                .containsExactly("AS-ALPHA-001");
        assertThat(numbersOf(asReceptionRepository.search(
                    null,
                    AsReceptionStatus.INSPECTION,
                    park.getId(),
                    PageRequest.of(0,
                    10
                ))))
                .containsExactly("AS-BETA-002");
        assertThat(asReceptionRepository.search(
                    null,
                    AsReceptionStatus.INSPECTION,
                    kim.getId(),
                    PageRequest.of(0,
                    10)
                ).getContent())
                .isEmpty();
    }

    @Test
    @DisplayName("결제, 배송, 수령, 완료 상태는 기사 재배정 대상에서 빠진다")
    void findByIdAndStatusNotIn_excludesClosedStatuses() {
        List<AsReceptionStatus> closedStatuses = List.of(
                AsReceptionStatus.AWAITING_PAYMENT,
                AsReceptionStatus.AWAITING_SHIPMENT,
                AsReceptionStatus.AWAITING_PICKUP,
                AsReceptionStatus.COMPLETED
        );

        assertThat(asReceptionRepository.findByIdAndStatusNotIn(alpha.getId(), closedStatuses)).isPresent();

        em.createQuery("update AsReception r set r.status = :status where r.id = :id")
                .setParameter("status", AsReceptionStatus.COMPLETED)
                .setParameter("id", alpha.getId())
                .executeUpdate();
        em.clear();

        assertThat(asReceptionRepository.findByIdAndStatusNotIn(alpha.getId(), closedStatuses)).isEmpty();
        assertThat(asReceptionRepository.findByIdAndStatusNotIn(999L, closedStatuses)).isEmpty();
    }

    @Test
    @DisplayName("페이지 크기만큼만 담고 전체 건수는 유지한다")
    void search_page() {
        Page<AsReceptionDto.ListResponse> result = asReceptionRepository.search(null, null, null, PageRequest.of(0, 1));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("고객 ID로 그 고객의 접수만 조회한다")
    void search_customerId() {
        assertThat(numbersOf(asReceptionRepository.search(
                null, null, null, alphaCustomerId, PageRequest.of(0, 10))))
                .containsExactly("AS-ALPHA-001");
    }

    private AsReception saveReception(
            String receptionNumber,
            Customer customer,
            Product product,
            Member receptionist,
            Member technician,
            String symptom
    ) {
        return asReceptionRepository.save(new AsReception(
                receptionNumber,
                customer,
                product,
                receptionist,
                technician,
                symptom,
                "상세 증상",
                "메모",
                ReturnMethod.DELIVERY,
                null
        ));
    }
}
