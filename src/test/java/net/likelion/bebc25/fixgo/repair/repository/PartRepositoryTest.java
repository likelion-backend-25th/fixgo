package net.likelion.bebc25.fixgo.repair.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartCompatibility;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class PartRepositoryTest {

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private EntityManager entityManager;

    private Long activeMotorId;
    private Long activeBracketId;
    private Long lowBearingId;
    private Long discontinuedId;

    @BeforeEach
    void setUp() {
        activeMotorId = save("모터", 1000, 20, 10, PartStatus.ACTIVE).getId();
        activeBracketId = save("모터 브라켓", 2000, 15, 4, PartStatus.ACTIVE).getId();
        lowBearingId = save("베어링", 3000, 10, 0, PartStatus.LOW_QUANTITY).getId();
        discontinuedId = save("단종 기판", 4000, 30, 1, PartStatus.DISCONTINUED).getId();
    }

    @Test
    @DisplayName("부품을 저장하고 id로 조회한다")
    void saveAndFindById() {
        Optional<Part> found = partRepository.findById(activeMotorId);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("모터");
        assertThat(found.get().getUnitPrice()).isEqualTo(1000);
        assertThat(found.get().getStandardMinutes()).isEqualTo(20);
        assertThat(found.get().getStockQuantity()).isEqualTo(10);
        assertThat(found.get().getStatus()).isEqualTo(PartStatus.ACTIVE);
        assertThat(found.get().getCreatedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이름에 포함하고 상태가 같은 부품을 조회한다")
    void searchParts_byNameAndStatus() {
        Page<Part> result = partRepository.searchParts("모터", PartStatus.ACTIVE, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Part::getId)
                .containsExactlyInAnyOrder(activeMotorId, activeBracketId);
    }

    @Test
    @DisplayName("상태가 없으면 이름만으로 조회한다")
    void searchParts_nameOnly() {
        Page<Part> result = partRepository.searchParts("모터", null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Part::getName)
                .containsExactlyInAnyOrder("모터", "모터 브라켓");
    }

    @Test
    @DisplayName("이름이 없으면 상태만으로 조회한다")
    void searchParts_statusOnly() {
        Page<Part> result = partRepository.searchParts(null, PartStatus.LOW_QUANTITY, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Part::getId)
                .containsExactly(lowBearingId);
    }

    @Test
    @DisplayName("조건이 모두 없으면 전체 부품을 조회한다")
    void searchParts_all() {
        Page<Part> result = partRepository.searchParts(null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("일치하는 부품이 없으면 빈 페이지를 반환한다")
    void searchParts_empty() {
        Page<Part> result = partRepository.searchParts("없는부품", PartStatus.ACTIVE, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("검색 결과는 페이지 크기를 따른다")
    void searchParts_page() {
        Page<Part> result = partRepository.searchParts(null, null, null, null, PageRequest.of(0, 1));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(4);
    }

    @Test
    @DisplayName("호환 제품군과 모델로 부품을 조회한다")
    void searchParts_byCompatibility() {
        Part board = save("메인보드", 1000, 10, 3, PartStatus.ACTIVE);
        Part battery = save("배터리", 2000, 10, 1, PartStatus.LOW_QUANTITY);
        ProductModel laptop = new ProductModel("사자북", "SZB-1", Category.LAPTOP, 12, 1L);
        ProductModel phone = new ProductModel("사자폰", "SZP-1", Category.PHONE, 12, 1L);
        entityManager.persist(laptop);
        entityManager.persist(phone);
        entityManager.persist(new PartCompatibility(board, laptop));
        entityManager.persist(new PartCompatibility(battery, phone));
        entityManager.flush();

        Page<Part> laptops = partRepository.searchParts(
                null, null, Category.LAPTOP, null, PageRequest.of(0, 10));
        Page<Part> matchedModel = partRepository.searchParts(
                null, null, null, laptop.getId(), PageRequest.of(0, 10));

        assertThat(laptops.getContent()).extracting(Part::getId).containsExactly(board.getId());
        assertThat(matchedModel.getContent()).extracting(Part::getId).containsExactly(board.getId());
    }

    @Test
    @DisplayName("id와 상태가 모두 일치하는 부품만 조회한다")
    void findByIdAndStatus() {
        assertThat(partRepository.findByIdAndStatus(discontinuedId, PartStatus.DISCONTINUED))
                .map(Part::getId)
                .contains(discontinuedId);
        assertThat(partRepository.findByIdAndStatus(discontinuedId, PartStatus.ACTIVE)).isEmpty();
    }

    @Test
    @DisplayName("지정한 상태가 아닌 부품만 조회한다")
    void findByIdAndStatusNot() {
        assertThat(partRepository.findByIdAndStatusNot(activeMotorId, PartStatus.DISCONTINUED))
                .map(Part::getId)
                .contains(activeMotorId);
        assertThat(partRepository.findByIdAndStatusNot(discontinuedId, PartStatus.DISCONTINUED)).isEmpty();
    }

    private Part save(String name, int unitPrice, int standardMinutes, int stockQuantity, PartStatus status) {
        Part part = new Part(name, unitPrice, standardMinutes, stockQuantity);
        ReflectionTestUtils.setField(part, "status", status);
        return partRepository.save(part);
    }
}
