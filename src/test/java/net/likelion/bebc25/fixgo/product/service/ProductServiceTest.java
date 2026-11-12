package net.likelion.bebc25.fixgo.product.service;

import net.likelion.bebc25.fixgo.product.dto.ProductDto;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import(ProductService.class)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductModelRepository productModelRepository;

    private ProductModel saveProductModel(String name, String modelName, Category category) {
        return productModelRepository.save(
                new ProductModel(name, modelName, category, 12, 1L)
        );
    }

    private Product saveProduct(Long productModelId, String serialNumber) {
        return productRepository.save(
                new Product(productModelId, serialNumber, LocalDateTime.of(2025, 5, 14, 0, 0), 1L)
        );
    }

    @Test
    @DisplayName("제품 ID로 상세 조회에 성공하고 모델 정보를 포함한다")
    void getById_success() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        Product product = saveProduct(model.getId(), "SZP24-22018-51542");

        ProductDto.Response response = productService.getById(product.getId());

        assertThat(response.id()).isEqualTo(product.getId());
        assertThat(response.serialNumber()).isEqualTo("SZP24-22018-51542");
        assertThat(response.manufacturedAt()).isEqualTo(LocalDateTime.of(2025, 5, 14, 0, 0));
        assertThat(response.purchasedAt()).isNull();
        assertThat(response.customerId()).isNull();
        assertThat(response.productModelId()).isEqualTo(model.getId());
        assertThat(response.name()).isEqualTo("스마트폰");
        assertThat(response.modelName()).isEqualTo("SZ-P24");
        assertThat(response.category()).isEqualTo(Category.PHONE);
        assertThat(response.warrantyMonths()).isEqualTo(12);
        assertThat(response.createdBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 제품을 조회하면 예외가 발생한다")
    void getById_fail() {
        assertThatThrownBy(() -> productService.getById(-1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 제품입니다.");
    }

    @Test
    @DisplayName("제품 등록에 성공하면 저장된 정보를 반환한다")
    void create_success() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        LocalDateTime manufacturedAt = LocalDateTime.of(2025, 5, 14, 0, 0);
        LocalDateTime purchasedAt = LocalDateTime.of(2025, 6, 1, 0, 0);
        ProductDto.CreateRequest request = new ProductDto.CreateRequest(
                model.getId(), 10L, "SZP24-22018-51542", manufacturedAt, purchasedAt
        );

        ProductDto.Response response = productService.create(request, 1L);

        assertThat(response.id()).isNotNull();
        assertThat(response.serialNumber()).isEqualTo("SZP24-22018-51542");
        assertThat(response.customerId()).isEqualTo(10L);
        assertThat(response.productModelId()).isEqualTo(model.getId());
        assertThat(response.manufacturedAt()).isEqualTo(manufacturedAt);
        assertThat(response.purchasedAt()).isEqualTo(purchasedAt);
        assertThat(response.name()).isEqualTo("스마트폰");
        assertThat(response.modelName()).isEqualTo("SZ-P24");
        assertThat(response.createdBy()).isEqualTo(1L);
        assertThat(response.updatedBy()).isEqualTo(1L);
        assertThat(productRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("이미 등록된 시리얼번호로 등록하면 예외가 발생한다")
    void create_duplicateSerial() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProduct(model.getId(), "SZP24-22018-51542");
        ProductDto.CreateRequest request = new ProductDto.CreateRequest(
                model.getId(), null, "SZP24-22018-51542",
                LocalDateTime.of(2025, 5, 14, 0, 0), null
        );

        assertThatThrownBy(() -> productService.create(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 시리얼번호입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 모델로 등록하면 예외가 발생한다")
    void create_modelNotFound() {
        ProductDto.CreateRequest request = new ProductDto.CreateRequest(
                999L, null, "SZP24-22018-51542",
                LocalDateTime.of(2025, 5, 14, 0, 0), null
        );

        assertThatThrownBy(() -> productService.create(request, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 모델입니다.");
    }

    @Test
    @DisplayName("시리얼번호 부분 일치로 검색한다")
    void search_bySerialNumber() {
        ProductModel phoneModel = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        ProductModel laptopModel = saveProductModel("노트북", "SZ-N16", Category.LAPTOP);
        saveProduct(phoneModel.getId(), "SZP24-22018-51542");
        saveProduct(phoneModel.getId(), "SZP24-22018-99999");
        saveProduct(laptopModel.getId(), "SZN16-11001-10001");

        List<ProductDto.Response> responses = productService.search(
                new ProductDto.SearchRequest("SZP24")
        );

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ProductDto.Response::serialNumber)
                .containsExactlyInAnyOrder("SZP24-22018-51542", "SZP24-22018-99999");
        assertThat(responses).allMatch(response -> response.modelName().equals("SZ-P24"));
    }

    @Test
    @DisplayName("일치하는 시리얼번호가 없으면 빈 목록을 반환한다")
    void search_noMatch_returnsEmpty() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProduct(model.getId(), "SZP24-22018-51542");

        List<ProductDto.Response> responses = productService.search(
                new ProductDto.SearchRequest("없는시리얼")
        );

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("시리얼번호가 비어 있으면 빈 목록을 반환한다")
    void search_blank_returnsEmpty() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProduct(model.getId(), "SZP24-22018-51542");

        assertThat(productService.search(new ProductDto.SearchRequest(null))).isEmpty();
        assertThat(productService.search(new ProductDto.SearchRequest("  "))).isEmpty();
    }

    @Test
    @DisplayName("시리얼번호가 비어 있으면 전체 제품을 페이징한다")
    void searchPage_blank_returnsAll() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProduct(model.getId(), "SZP24-22018-51542");
        saveProduct(model.getId(), "SZP24-22018-99999");

        Page<ProductDto.Response> page = productService.searchPage(
                new ProductDto.SearchRequest("  "),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(page.getContent()).extracting(ProductDto.Response::serialNumber)
                .containsExactly("SZP24-22018-99999", "SZP24-22018-51542");
    }

    @Test
    @DisplayName("제품 정보를 수정한다")
    void update_success() {
        ProductModel phone = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        ProductModel laptop = saveProductModel("노트북", "SZ-N16", Category.LAPTOP);
        Product saved = saveProduct(phone.getId(), "SZP24-22018-51542");
        LocalDateTime manufacturedAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime purchasedAt = LocalDateTime.of(2024, 2, 1, 0, 0);

        ProductDto.UpdateRequest request = new ProductDto.UpdateRequest(
                laptop.getId(), 20L, "SZN16-UPDATED-001", manufacturedAt, purchasedAt
        );

        ProductDto.Response response = productService.update(saved.getId(), request, 2L);

        assertThat(response.productModelId()).isEqualTo(laptop.getId());
        assertThat(response.customerId()).isEqualTo(20L);
        assertThat(response.serialNumber()).isEqualTo("SZN16-UPDATED-001");
        assertThat(response.manufacturedAt()).isEqualTo(manufacturedAt);
        assertThat(response.purchasedAt()).isEqualTo(purchasedAt);
        assertThat(response.modelName()).isEqualTo("SZ-N16");
        assertThat(response.updatedBy()).isEqualTo(2L);

        Product updated = productRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getCustomerId()).isEqualTo(20L);
        assertThat(updated.getSerialNumber()).isEqualTo("SZN16-UPDATED-001");
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 제품을 수정하면 예외가 발생한다")
    void update_notFound() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        ProductDto.UpdateRequest request = new ProductDto.UpdateRequest(
                model.getId(), null, "SN-1",
                LocalDateTime.of(2025, 5, 14, 0, 0), null
        );

        assertThatThrownBy(() -> productService.update(999L, request, 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 제품입니다.");
    }
}
