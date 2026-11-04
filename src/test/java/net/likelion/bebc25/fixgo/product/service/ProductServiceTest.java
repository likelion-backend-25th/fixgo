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
    }

    @Test
    @DisplayName("존재하지 않는 제품을 조회하면 예외가 발생한다")
    void getById_fail() {
        assertThatThrownBy(() -> productService.getById(-1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 제품입니다.");
    }

    @Test
    @DisplayName("시리얼번호 완전 일치로 검색한다")
    void search_bySerialNumber() {
        ProductModel phoneModel = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        ProductModel laptopModel = saveProductModel("노트북", "SZ-N16", Category.LAPTOP);
        saveProduct(phoneModel.getId(), "SZP24-22018-51542");
        saveProduct(laptopModel.getId(), "SZN16-11001-10001");

        List<ProductDto.Response> responses = productService.search(
                new ProductDto.SearchRequest("SZP24-22018-51542")
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().serialNumber()).isEqualTo("SZP24-22018-51542");
        assertThat(responses.getFirst().modelName()).isEqualTo("SZ-P24");
        assertThat(responses.getFirst().category()).isEqualTo(Category.PHONE);
        assertThat(responses.getFirst().name()).isEqualTo("스마트폰");
    }

    @Test
    @DisplayName("일치하는 시리얼번호가 없으면 빈 목록을 반환한다")
    void search_noMatch_returnsEmpty() {
        ProductModel model = saveProductModel("스마트폰", "SZ-P24", Category.PHONE);
        saveProduct(model.getId(), "SZP24-22018-51542");

        List<ProductDto.Response> responses = productService.search(
                new ProductDto.SearchRequest("SZP24")
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
}
