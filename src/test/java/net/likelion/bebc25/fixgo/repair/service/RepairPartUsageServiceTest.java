package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageResponse;
import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStockHistory;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.repository.PartRepository;
import net.likelion.bebc25.fixgo.repair.repository.PartStockHistoryRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairPartUsageServiceTest {

    @Mock
    private RepairPartUsageRepository repairPartUsageRepository;

    @Mock
    private RepairWorkRepository repairWorkRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private PartStockHistoryRepository partStockHistoryRepository;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private RepairWork repairWork;

    @Mock
    private Part part;

    @InjectMocks
    private RepairPartUsageService repairPartUsageService;

    @Test
    @DisplayName("실제 부품 사용 등록 성공 - 사용 내역 저장, 재고 차감, 출고 이력 저장")
    void createUsage_success() {
        // given
        RepairPartUsageRequest request =
                new RepairPartUsageRequest(1L, 3L, 2);

        given(userDetails.getId()).willReturn(10L);
        given(repairWorkRepository.findById(1L))
                .willReturn(Optional.of(repairWork));

        given(partRepository.findByIdAndStatusNot(3L, PartStatus.DISCONTINUED))
                .willReturn(Optional.of(part));

        given(part.getStockQuantity()).willReturn(10);
        given(part.getUnitPrice()).willReturn(150000);
        given(part.getName()).willReturn("노트북 메인보드");
        given(part.getId()).willReturn(3L);
        given(repairWork.getId()).willReturn(1L);

        given(repairPartUsageRepository.save(any(RepairPartUsage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        RepairPartUsageResponse response =
                repairPartUsageService.createUsage(userDetails, request);

        // then
        assertThat(response.repairWorkId()).isEqualTo(1L);
        assertThat(response.partId()).isEqualTo(3L);
        assertThat(response.partName()).isEqualTo("노트북 메인보드");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.unitPrice()).isEqualTo(150000);
        assertThat(response.totalPrice()).isEqualTo(300000);

        // 재고 차감 확인
        verify(part).updateStockQuantity(8);

        // 실제 사용 부품 저장 확인
        ArgumentCaptor<RepairPartUsage> usageCaptor =
                ArgumentCaptor.forClass(RepairPartUsage.class);

        verify(repairPartUsageRepository).save(usageCaptor.capture());

        RepairPartUsage savedUsage = usageCaptor.getValue();

        assertThat(savedUsage.getRepairWork()).isEqualTo(repairWork);
        assertThat(savedUsage.getPart()).isEqualTo(part);
        assertThat(savedUsage.getQuantity()).isEqualTo(2);
        assertThat(savedUsage.getUnitPrice()).isEqualTo(150000);

        // 출고 이력 저장 확인
        ArgumentCaptor<PartStockHistory> historyCaptor =
                ArgumentCaptor.forClass(PartStockHistory.class);

        verify(partStockHistoryRepository).save(historyCaptor.capture());

        PartStockHistory savedHistory = historyCaptor.getValue();

        assertThat(savedHistory.getPart()).isEqualTo(part);
        assertThat(savedHistory.getRepairPartUsage()).isEqualTo(savedUsage);
        assertThat(savedHistory.getMovementType()).isEqualTo(MovementType.OUT);
        assertThat(savedHistory.getQuantityDelta()).isEqualTo(-2);
        assertThat(savedHistory.getReason()).isEqualTo("수리 부품 사용");
    }

    @Test
    @DisplayName("사용 수량이 0 이하이면 예외 발생")
    void createUsage_invalidQuantity() {
        // given
        RepairPartUsageRequest request =
                new RepairPartUsageRequest(1L, 3L, 0);

        // when & then
        assertThatThrownBy(() ->
                                   repairPartUsageService.createUsage(userDetails, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("부품 사용 수량은 1개 이상이어야 합니다.");

        verifyNoInteractions(
                repairWorkRepository,
                partRepository,
                repairPartUsageRepository,
                partStockHistoryRepository
        );
    }

    @Test
    @DisplayName("존재하지 않는 수리 작업이면 예외 발생")
    void createUsage_repairWorkNotFound() {
        // given
        RepairPartUsageRequest request =
                new RepairPartUsageRequest(1L, 3L, 2);

        given(repairWorkRepository.findById(1L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                                   repairPartUsageService.createUsage(userDetails, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수리 작업을 찾을 수 없습니다.");

        verify(repairPartUsageRepository, never()).save(any());
        verify(partStockHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않거나 단종된 부품이면 예외 발생")
    void createUsage_partNotFound() {
        // given
        RepairPartUsageRequest request =
                new RepairPartUsageRequest(1L, 3L, 2);

        given(repairWorkRepository.findById(1L))
                .willReturn(Optional.of(repairWork));

        given(partRepository.findByIdAndStatusNot(3L, PartStatus.DISCONTINUED))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                                   repairPartUsageService.createUsage(userDetails, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 수 있는 부품을 찾을 수 없습니다.");

        verify(repairPartUsageRepository, never()).save(any());
        verify(partStockHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("재고가 부족하면 예외 발생")
    void createUsage_insufficientStock() {
        // given
        RepairPartUsageRequest request =
                new RepairPartUsageRequest(1L, 3L, 5);

        given(repairWorkRepository.findById(1L))
                .willReturn(Optional.of(repairWork));

        given(partRepository.findByIdAndStatusNot(3L, PartStatus.DISCONTINUED))
                .willReturn(Optional.of(part));

        given(part.getStockQuantity()).willReturn(3);

        // when & then
        assertThatThrownBy(() ->
                                   repairPartUsageService.createUsage(userDetails, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("부품 재고가 부족합니다.");

        verify(part, never()).updateStockQuantity(anyInt());
        verify(repairPartUsageRepository, never()).save(any());
        verify(partStockHistoryRepository, never()).save(any());
    }
}