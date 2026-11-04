package net.likelion.bebc25.fixgo.LaborRate.repository;

import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.product.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class LaborRateRepositoryTest {

    @Autowired
    private LaborRateRepository laborRateRepository;

    @Test
    @DisplayName("제품군과 요금 종류에 해당하는 최신 적용 요금을 조회한다.")
    void findLatestRate() {

        // given
        Category category = Category.values()[0];

        LocalDate today = LocalDate.now();

        LaborRate oldRate = new LaborRate(category, 10000, RateType.LABOR_RATE, today.minusMonths(2));

        LaborRate latestRate = new LaborRate(category, 20000, RateType.LABOR_RATE, today.minusMonths(1));

        // 아직 적용되지 않은 미래 요금
        LaborRate futureRate = new LaborRate(category, 30000, RateType.LABOR_RATE, today.plusMonths(1));

        laborRateRepository.save(oldRate);
        laborRateRepository.save(latestRate);
        laborRateRepository.save(futureRate);

        // when
        LaborRate result = laborRateRepository.findLatestRate(category, RateType.LABOR_RATE, today, PageRequest.of(0, 1)).stream().findFirst().orElseThrow();

        // then
        assertThat(result.getCost()).isEqualTo(20000);
        assertThat(result.getEffectiveDate()).isEqualTo(today.minusMonths(1));
    }

    @Test
    @DisplayName("적용 가능한 요금이 없으면 빈 결과를 반환한다.")
    void findLatestRateNotFound() {

        // given
        Category category = Category.values()[0];

        // when
        var result = laborRateRepository.findLatestRate(category, RateType.INSPECTION_COST, LocalDate.now(), PageRequest.of(0, 1));

        // then
        assertThat(result).isEmpty();
    }
}
