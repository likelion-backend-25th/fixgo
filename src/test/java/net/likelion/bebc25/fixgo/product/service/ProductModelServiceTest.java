package net.likelion.bebc25.fixgo.product.service;

import net.likelion.bebc25.fixgo.product.dto.ProductModelDto;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import(ProductModelService.class)
public class ProductModelServiceTest {

    @Autowired
    private ProductModelService productModelService;

    @Autowired
    private ProductModelRepository productModelRepository;

    private ProductModel saveProductModel(String name, String modelName, Category category) {
        return productModelRepository.save(
                new ProductModel(name, modelName, category, 12, 1L)
        );
    }

    @Test
    @DisplayName("모델 ID로 상세 조회에 성공한다")
    void getById_success() {
        ProductModel productModel = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);

        ProductModelDto.Response response = productModelService.getById(productModel.getId());

        assertThat(response.id()).isEqualTo(productModel.getId());
        assertThat(response.name()).isEqualTo("스마트폰");
        assertThat(response.modelName()).isEqualTo("SZ-P24");
        assertThat(response.category()).isEqualTo(Category.PHONE);
        assertThat(response.warrantyMonths()).isEqualTo(12);
        assertThat(response.isActive()).isTrue();
        assertThat(response.createdBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 모델을 조회하면 예외가 발생한다")
    void getById_fail() {
        assertThatThrownBy(() -> productModelService.getById(-1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 모델입니다.");
    }

    @Test
    @DisplayName("모델 등록에 성공하면 저장된 정보를 반환한다")
    void create_success() {
        ProductModelDto.CreateRequest request = new ProductModelDto.CreateRequest(
                "노트북", "SZ-N16", Category.LAPTOP, 24
        );

        ProductModelDto.Response response = productModelService.create(request, 1L);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("노트북");
        assertThat(response.modelName()).isEqualTo("SZ-N16");
        assertThat(response.category()).isEqualTo(Category.LAPTOP);
        assertThat(response.warrantyMonths()).isEqualTo(24);
        assertThat(response.isActive()).isTrue();
        assertThat(response.createdBy()).isEqualTo(1L);
        assertThat(response.updatedBy()).isEqualTo(1L);
        assertThat(productModelRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("keyword로 제품명을 검색한다")
    void search_byNameKeyword() {
        saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProductModel("스마트폰 프로", "SZ-P25", Category.PHONE);
        saveProductModel("노트북", "SZ-N16", Category.LAPTOP);

        Page<ProductModelDto.Response> page = productModelService.search(
                "스마트", PageRequest.of(0, 20, Sort.by("modelName").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(ProductModelDto.Response::name)
                .containsExactlyInAnyOrder("스마트폰", "스마트폰 프로");
    }

    @Test
    @DisplayName("keyword로 모델명을 검색한다")
    void search_byModelNameKeyword() {
        saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProductModel("스마트폰 프로", "SZ-P25", Category.PHONE);
        saveProductModel("노트북", "SZ-N16", Category.LAPTOP);

        Page<ProductModelDto.Response> page = productModelService.search(
                "SZ-P", PageRequest.of(0, 20, Sort.by("modelName").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(ProductModelDto.Response::modelName)
                .containsExactlyInAnyOrder("SZ-P24", "SZ-P25");
    }

    @Test
    @DisplayName("keyword가 없으면 전체 목록을 페이징하여 반환한다")
    void search_withoutKeyword_returnsAllPaged() {
        saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProductModel("스마트폰 프로", "SZ-P25", Category.PHONE);
        saveProductModel("노트북", "SZ-N16", Category.LAPTOP);

        Page<ProductModelDto.Response> page = productModelService.search(
                null, PageRequest.of(0, 2, Sort.by("modelName").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(ProductModelDto.Response::modelName)
                .containsExactly("SZ-N16", "SZ-P24");
    }

    @Test
    @DisplayName("keyword가 빈 문자열이면 전체 목록을 페이징하여 반환한다")
    void search_emptyKeyword_returnsAllPaged() {
        saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProductModel("노트북", "SZ-N16", Category.LAPTOP);

        Page<ProductModelDto.Response> page = productModelService.search(
                "", PageRequest.of(0, 20)
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(ProductModelDto.Response::name)
                .containsExactlyInAnyOrder("스마트폰", "노트북");
    }

    @Test
    @DisplayName("keyword에 해당하는 모델이 없으면 빈 페이지를 반환한다")
    void search_noMatch_returnsEmptyPage() {
        saveProductModel("스마트폰", "SZ-P24", Category.PHONE);

        Page<ProductModelDto.Response> page = productModelService.search(
                "없는키워드", PageRequest.of(0, 20)
        );

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("모델 정보를 수정한다")
    void update_success() {
        ProductModel saved = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);

        ProductModelDto.UpdateRequest request = new ProductModelDto.UpdateRequest(
                "스마트폰 수정", "SZ-P24X", Category.PHONE, 36, false
        );

        ProductModelDto.Response response = productModelService.update(saved.getId(), request, 2L);

        assertThat(response.name()).isEqualTo("스마트폰 수정");
        assertThat(response.modelName()).isEqualTo("SZ-P24X");
        assertThat(response.category()).isEqualTo(Category.PHONE);
        assertThat(response.warrantyMonths()).isEqualTo(36);
        assertThat(response.isActive()).isFalse();
        assertThat(response.updatedBy()).isEqualTo(2L);

        ProductModel updated = productModelRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("스마트폰 수정");
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 모델을 수정하면 예외가 발생한다")
    void update_notFound() {
        ProductModelDto.UpdateRequest request = new ProductModelDto.UpdateRequest(
                "이름", "MODEL-1", Category.PHONE, 12, true
        );

        assertThatThrownBy(() -> productModelService.update(999L, request, 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 모델입니다.");
    }
}