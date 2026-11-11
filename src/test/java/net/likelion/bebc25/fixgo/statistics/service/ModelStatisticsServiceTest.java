package net.likelion.bebc25.fixgo.statistics.service;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;
import net.likelion.bebc25.fixgo.statistics.dto.ModelStatisticsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ModelStatisticsService.class, JpaConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ModelStatisticsServiceTest {

    private static final String PREFIX = "MODEL-SERVICE-STAT-";

    @Autowired
    ModelStatisticsService modelStatisticsService;

    @Autowired
    EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM AsReception ar WHERE ar.receptionNumber LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();

            entityManager.createQuery("DELETE FROM Product p WHERE p.serialNumber LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();

            entityManager.createQuery("DELETE FROM Customer c WHERE c.email LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();

            entityManager.createQuery("DELETE FROM Member m WHERE m.email LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();

            entityManager.createQuery("DELETE FROM ProductModel pm WHERE pm.modelName LIKE :prefix")
                         .setParameter("prefix", PREFIX + "%").executeUpdate();
        });
    }

    @Test
    @DisplayName("모델별 접수, 완료, 진행 건수와 주요 증상을 조회한다.")
    void getModelStatistics() {

        // given
        createTestData();

        // when
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        List<ModelStatisticsDto.ModelStatisticsResponse> statistics = modelStatisticsService.getModelStatistics(
                startDate, endDate);

        // then
        ModelStatisticsDto.ModelStatisticsResponse galaxy = statistics.stream().filter(item -> item.modelName()
                                                                                                   .equals(PREFIX + "GALAXY"))
                                                                      .findFirst().orElseThrow();

        assertThat(galaxy.modelId()).isNotNull();
        assertThat(galaxy.productGroup()).isEqualTo("PHONE");
        assertThat(galaxy.receptionCount()).isEqualTo(3L);
        assertThat(galaxy.completedCount()).isEqualTo(1L);
        assertThat(galaxy.inProgressCount()).isEqualTo(2L);
        assertThat(galaxy.mainSymptom()).isEqualTo("배터리");
    }

    @Test
    @DisplayName("특정 모델의 증상별 접수 건수와 비율을 조회한다.")
    void getModelSymptomStatistics() {

        // given
        Long modelId = createTestData();

        // when
        List<ModelStatisticsDto.ModelSymptomStatisticsResponse> symptoms = modelStatisticsService.getModelSymptomStatistics(
                modelId, LocalDate.now().minusDays(1), LocalDate.now());

        // then
        assertThat(symptoms).hasSize(2);

        assertThat(symptoms).extracting(ModelStatisticsDto.ModelSymptomStatisticsResponse::symptomName)
                            .containsExactly("배터리", "충전");

        assertThat(symptoms).extracting(ModelStatisticsDto.ModelSymptomStatisticsResponse::count)
                            .containsExactly(2L, 1L);

        assertThat(symptoms).extracting(ModelStatisticsDto.ModelSymptomStatisticsResponse::percentage)
                            .containsExactly(66.67, 33.33);
    }

    @Test
    @DisplayName("접수 내역이 없는 모델의 주요 증상은 null이다.")
    void getModelStatisticsWithoutReception() {

        // given
        createTestData();

        // when
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        List<ModelStatisticsDto.ModelStatisticsResponse> statistics = modelStatisticsService.getModelStatistics(
                startDate, endDate);

        // then
        ModelStatisticsDto.ModelStatisticsResponse emptyModel = statistics.stream().filter(item -> item.modelName()
                                                                                                       .equals(PREFIX + "EMPTY"))
                                                                          .findFirst().orElseThrow();

        assertThat(emptyModel.receptionCount()).isZero();
        assertThat(emptyModel.completedCount()).isZero();
        assertThat(emptyModel.inProgressCount()).isZero();
        assertThat(emptyModel.mainSymptom()).isNull();
    }

    private Long createTestData() {
        return transactionTemplate.execute(status -> {

            // 모델 생성
            ProductModel model = new ProductModel("Galaxy", PREFIX + "GALAXY", Category.PHONE, 24, 1L);

            ProductModel emptyModel = new ProductModel("Empty", PREFIX + "EMPTY", Category.LAPTOP, 12, 1L);

            entityManager.persist(model);
            entityManager.persist(emptyModel);
            entityManager.flush();

            // 제품 생성
            Product product = new Product(model.getId(), PREFIX + "SERIAL-1", LocalDateTime.now(), 1L);

            entityManager.persist(product);

            // 고객 생성
            Customer customer = new Customer("테스트 고객", "010-1234-5678", PREFIX + "customer@test.com", "12345", "서울시",
                                             "테스트 주소", 1L);

            entityManager.persist(customer);

            // 기사 및 접수 담당자 생성
            Member receptionist = new Member(
                    PREFIX + "admin@test.com",
                    "password",
                    "접수 담당자",
                    "010-1111-1111",
                    "서울시 강남구",
                    Role.ROLE_ADMIN,
                    ""
            );

            Member technician = new Member(
                    PREFIX + "tech@test.com",
                    "password",
                    "테스트 기사",
                    "010-2222-2222",
                    "서울시 서초구",
                    Role.ROLE_TECHNICIAN,
                    ""
            );

            entityManager.persist(receptionist);
            entityManager.persist(technician);
            entityManager.flush();

            // 접수 3건 생성
            entityManager.persist(createReception("1", customer, product, receptionist, technician, "배터리"));

            entityManager.persist(createReception("2", customer, product, receptionist, technician, "배터리"));

            entityManager.persist(createReception("3", customer, product, receptionist, technician, "충전"));

            entityManager.flush();

            // 첫 번째 접수를 완료 상태로 변경
            entityManager.createQuery("""
                                              UPDATE AsReception ar
                                              SET ar.status = :completedStatus
                                              WHERE ar.receptionNumber = :receptionNumber
                                              """).setParameter("completedStatus", AsReceptionStatus.COMPLETED)
                         .setParameter("receptionNumber", PREFIX + "RECEPTION-1").executeUpdate();

            return model.getId();
        });
    }

    private AsReception createReception(
            String number, Customer customer, Product product, Member receptionist,
            Member technician, String symptom
    ) {
        return new AsReception(PREFIX + "RECEPTION-" + number, customer, product, receptionist, technician, symptom,
                               symptom + " 상세 증상", null, ReturnMethod.DELIVERY, null);
    }
}