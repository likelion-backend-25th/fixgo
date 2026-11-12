package net.likelion.bebc25.fixgo.admin.service;

import net.likelion.bebc25.fixgo.admin.dto.LaborRateDto;
import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import net.likelion.bebc25.fixgo.product.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaborRateServiceTest {

    @Mock
    private LaborRateRepository laborRateRepository;

    @InjectMocks
    private LaborRateService laborRateService;

    @Test
    @DisplayName("공임비·점검비 전체 목록을 조회한다.")
    void getAllLaborRates() {
        LaborRate laborRate = laborRate(1L, Category.LAPTOP, 50000, RateType.LABOR_RATE, LocalDate.of(2025, 1, 1));
        LaborRate inspectionRate = laborRate(2L, Category.LAPTOP, 20000, RateType.INSPECTION_COST, LocalDate.of(2025, 1, 1));
        when(laborRateRepository.findAllByOrderByProductCategoryAscRateTypeAsc())
                .thenReturn(List.of(inspectionRate, laborRate));

        List<LaborRateDto.Response> result = laborRateService.getAllLaborRates();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().rateType()).isEqualTo(RateType.INSPECTION_COST);
        assertThat(result.get(1).rateType()).isEqualTo(RateType.LABOR_RATE);
    }

    @Test
    @DisplayName("요금 ID로 상세 정보를 조회한다.")
    void getLaborRate() {
        LaborRate laborRate = laborRate(1L, Category.PHONE, 15000, RateType.INSPECTION_COST, LocalDate.of(2025, 1, 1));
        when(laborRateRepository.findById(1L)).thenReturn(Optional.of(laborRate));

        LaborRateDto.Response result = laborRateService.getLaborRate(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.productCategory()).isEqualTo(Category.PHONE);
        assertThat(result.cost()).isEqualTo(15000);
    }

    @Test
    @DisplayName("존재하지 않는 요금 ID 조회 시 예외가 발생한다.")
    void getLaborRateNotFound() {
        when(laborRateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> laborRateService.getLaborRate(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 요금 정보입니다.");
    }

    @Test
    @DisplayName("공임비·점검비를 수정한다.")
    void updateLaborRate() {
        LaborRate laborRate = laborRate(1L, Category.PHONE, 35000, RateType.LABOR_RATE, LocalDate.of(2025, 1, 1));
        when(laborRateRepository.findById(1L)).thenReturn(Optional.of(laborRate));

        LaborRateDto.Response result = laborRateService.updateLaborRate(
                new LaborRateDto.UpdateRequest(1L, 40000)
        );

        assertThat(result.cost()).isEqualTo(40000);
        assertThat(result.effectiveDate()).isEqualTo(LocalDate.now());
        assertThat(laborRate.getCost()).isEqualTo(40000);
        assertThat(laborRate.getEffectiveDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("비용이 음수이면 수정 시 예외가 발생한다.")
    void updateLaborRateInvalidCost() {
        assertThatThrownBy(() -> laborRateService.updateLaborRate(
                new LaborRateDto.UpdateRequest(1L, -1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비용은 0 이상이어야 합니다.");
    }

    private LaborRate laborRate(
            Long id,
            Category category,
            int cost,
            RateType rateType,
            LocalDate effectiveDate
    ) {
        LaborRate laborRate = new LaborRate(category, cost, rateType, effectiveDate);
        ReflectionTestUtils.setField(laborRate, "id", id);
        ReflectionTestUtils.setField(laborRate, "createdAt", LocalDateTime.of(2025, 1, 1, 9, 0));
        ReflectionTestUtils.setField(laborRate, "updatedAt", LocalDateTime.of(2025, 1, 1, 9, 0));
        ReflectionTestUtils.setField(laborRate, "createdBy", 1L);
        ReflectionTestUtils.setField(laborRate, "updatedBy", 1L);
        return laborRate;
    }
}
