package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.part.PartDto;
import net.likelion.bebc25.fixgo.repair.entity.MovementType;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStockHistory;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private RepairWorkRepository repairWorkRepository;

    @Mock
    private RepairPartUsageRepository repairPartUsageRepository;

    @Mock
    private PartStockHistoryRepository partStockHistoryRepository;

    @Mock
    private RepairRepository repairRepository;

    @InjectMocks
    private PartService partService;

    @Test
    @DisplayName("부품명과 상태로 목록을 조회한다")
    void searchParts() {
        Part part = part(1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);
        when(partRepository.searchParts("메인", PartStatus.ACTIVE, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(part), pageable, 1));

        Page<PartDto.BriefResponse> result = partService.searchParts(
                new PartDto.SearchRequest("메인", PartStatus.ACTIVE, null, null), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(
                new PartDto.BriefResponse(1L, "메인보드", 15000, 8));
    }

    @Test
    @DisplayName("부품 상세 조회에 성공한다")
    void getPartsById_success() {
        Part part = part(1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE);
        ReflectionTestUtils.setField(part, "version", 2L);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        PartDto.DetailResponse result = partService.getPartsById(1L);

        assertThat(result).isEqualTo(new PartDto.DetailResponse(
                1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE, 2L));
    }

    @Test
    @DisplayName("존재하지 않는 부품을 조회하면 예외가 발생한다")
    void getPartsById_notFound() {
        when(partRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partService.getPartsById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 부품입니다.");
    }

    @Test
    @DisplayName("단가를 변경한다")
    void updateUnitPrice_success() {
        Part part = part(1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        partService.updateUnitPrice(new PartDto.PriceUpdateRequest(1L, 0));

        assertThat(part.getUnitPrice()).isZero();
    }

    @Test
    @DisplayName("단가를 음수로 변경하면 예외가 발생한다")
    void updateUnitPrice_negative() {
        Part part = part(1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        assertThatThrownBy(() -> partService.updateUnitPrice(new PartDto.PriceUpdateRequest(1L, -1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("단가를 0이하로 설정할 수 없습니다.");
        assertThat(part.getUnitPrice()).isEqualTo(15000);
    }

    @Test
    @DisplayName("존재하지 않는 부품의 단가를 변경하면 예외가 발생한다")
    void updateUnitPrice_notFound() {
        when(partRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partService.updateUnitPrice(new PartDto.PriceUpdateRequest(99L, 1000)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 부품입니다.");
    }

    @Test
    @DisplayName("기준 시간을 변경한다")
    void updateStandardMinutes_success() {
        Part part = part(1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        partService.updateStandardMinutes(new PartDto.StandardMinutesUpdateRequest(1L, 0));

        assertThat(part.getStandardMinutes()).isZero();
    }

    @Test
    @DisplayName("기준 시간을 음수로 변경하면 예외가 발생한다")
    void updateStandardMinutes_negative() {
        Part part = part(1L, "메인보드", 15000, 40, 8, PartStatus.ACTIVE);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        assertThatThrownBy(() -> partService.updateStandardMinutes(
                new PartDto.StandardMinutesUpdateRequest(1L, -1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기준 시간은 0이하로 설정할 수 없습니다.");
        assertThat(part.getStandardMinutes()).isEqualTo(40);
    }

    @Test
    @DisplayName("존재하지 않는 부품의 기준 시간을 변경하면 예외가 발생한다")
    void updateStandardMinutes_notFound() {
        when(partRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partService.updateStandardMinutes(
                new PartDto.StandardMinutesUpdateRequest(99L, 10)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 부품입니다.");
    }

    @Test
    @DisplayName("입고하면 재고가 늘고 이력이 저장된다")
    void updateSingleStockQuantity_in() {
        Part part = part(1L, "메인보드", 15000, 40, 2, PartStatus.LOW_QUANTITY);
        when(partRepository.findByIdAndStatusNot(1L, PartStatus.DISCONTINUED)).thenReturn(Optional.of(part));
        CustomUserDetails user = user(7L);

        partService.updateSingleStockQuantity(user, new PartDto.SingleStockUpdateRequest(
                1L, MovementType.IN, 3, "입고"));

        assertThat(part.getStockQuantity()).isEqualTo(5);
        assertThat(part.getStatus()).isEqualTo(PartStatus.ACTIVE);
        PartStockHistory history = capturedHistory();
        assertThat(history.getPart()).isSameAs(part);
        assertThat(history.getMovementType()).isEqualTo(MovementType.IN);
        assertThat(history.getQuantityDelta()).isEqualTo(3);
        assertThat(history.getReason()).isEqualTo("입고");
        assertThat(history.getCreatedBy()).isEqualTo(7L);
    }

    @Test
    @DisplayName("출고하면 재고가 줄고 재고가 0이면 상태가 LOW_QUANTITY가 된다")
    void updateSingleStockQuantity_outToZero() {
        Part part = part(1L, "메인보드", 15000, 40, 2, PartStatus.ACTIVE);
        when(partRepository.findByIdAndStatusNot(1L, PartStatus.DISCONTINUED)).thenReturn(Optional.of(part));

        partService.updateSingleStockQuantity(user(7L), new PartDto.SingleStockUpdateRequest(
                1L, MovementType.OUT, 2, "손실"));

        assertThat(part.getStockQuantity()).isZero();
        assertThat(part.getStatus()).isEqualTo(PartStatus.LOW_QUANTITY);
        assertThat(capturedHistory().getMovementType()).isEqualTo(MovementType.OUT);
    }

    @Test
    @DisplayName("입고나 출고가 아닌 이동은 재고를 차감한다")
    void updateSingleStockQuantity_adjust() {
        Part part = part(1L, "메인보드", 15000, 40, 5, PartStatus.ACTIVE);
        when(partRepository.findByIdAndStatusNot(1L, PartStatus.DISCONTINUED)).thenReturn(Optional.of(part));

        partService.updateSingleStockQuantity(user(7L), new PartDto.SingleStockUpdateRequest(
                1L, MovementType.ADJUST, 1, "조정"));

        assertThat(part.getStockQuantity()).isEqualTo(4);
        assertThat(part.getStatus()).isEqualTo(PartStatus.ACTIVE);
    }

    @Test
    @DisplayName("보유 재고보다 많이 출고하면 예외가 발생한다")
    void updateSingleStockQuantity_insufficient() {
        Part part = part(1L, "메인보드", 15000, 40, 1, PartStatus.ACTIVE);
        when(partRepository.findByIdAndStatusNot(1L, PartStatus.DISCONTINUED)).thenReturn(Optional.of(part));

        assertThatThrownBy(() -> partService.updateSingleStockQuantity(user(7L),
                                                                       new PartDto.SingleStockUpdateRequest(1L, MovementType.OUT, 2, "손실")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
        assertThat(part.getStockQuantity()).isEqualTo(1);
        verify(partStockHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("단종됐거나 없는 부품의 재고는 수정할 수 없다")
    void updateSingleStockQuantity_unavailable() {
        when(partRepository.findByIdAndStatusNot(1L, PartStatus.DISCONTINUED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partService.updateSingleStockQuantity(user(7L),
                                                                       new PartDto.SingleStockUpdateRequest(1L, MovementType.IN, 1, "입고")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않거나 사용 불가능한 부품입니다.");
    }

    @Test
    @DisplayName("완료되지 않은 수리는 재고 일괄 반영을 거절한다")
    void updateMultiStockQuantity_notCompleted() {
        when(repairRepository.existsByIdAndStatus(10L, RepairStatus.COMPLETED)).thenReturn(false);

        assertThatThrownBy(() -> partService.updateMultiStockQuantity(user(7L), 10L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않거나 아직 완료되지 않은 수리 건입니다.");
        verify(repairWorkRepository, never()).findByRepair_Id(10L);
    }

    @Test
    @DisplayName("완료된 수리의 사용 부품 수량만큼 재고를 차감하고 이력을 남긴다")
    void updateMultiStockQuantity_deductsUsage() {
        when(repairRepository.existsByIdAndStatus(10L, RepairStatus.COMPLETED)).thenReturn(true);
        RepairWork work = mock(RepairWork.class);
        when(work.getId()).thenReturn(20L);
        when(repairWorkRepository.findByRepair_Id(10L)).thenReturn(List.of(work));

        Part motor = part(1L, "모터", 1000, 10, 5, PartStatus.ACTIVE);
        Part bolt = part(2L, "볼트", 500, 5, 4, PartStatus.ACTIVE);
        RepairPartUsage motorUsage = usage(motor, 2);
        RepairPartUsage boltUsage = usage(bolt, 1);
        when(repairPartUsageRepository.findByRepairWorkIdIn(List.of(20L)))
                .thenReturn(List.of(motorUsage, boltUsage));
        when(partRepository.findByIdAndStatus(1L, PartStatus.ACTIVE)).thenReturn(Optional.of(motor));
        when(partRepository.findByIdAndStatus(2L, PartStatus.ACTIVE)).thenReturn(Optional.of(bolt));

        partService.updateMultiStockQuantity(user(7L), 10L);

        assertThat(motor.getStockQuantity()).isEqualTo(3);
        assertThat(bolt.getStockQuantity()).isEqualTo(3);
        assertThat(motor.getStatus()).isEqualTo(PartStatus.ACTIVE);

        ArgumentCaptor<PartStockHistory> captor = ArgumentCaptor.forClass(PartStockHistory.class);
        verify(partStockHistoryRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(PartStockHistory::getQuantityDelta)
                .containsExactly(2, 1);
        assertThat(captor.getAllValues()).allSatisfy(history -> {
            assertThat(history.getMovementType()).isEqualTo(MovementType.OUT);
            assertThat(history.getReason()).isNull();
            assertThat(history.getCreatedBy()).isEqualTo(7L);
        });
        assertThat(captor.getAllValues().getFirst().getRepairPartUsage()).isSameAs(motorUsage);
    }

    @Test
    @DisplayName("사용 가능한 부품이 아니면 재고 일괄 반영을 중단한다")
    void updateMultiStockQuantity_partUnavailable() {
        when(repairRepository.existsByIdAndStatus(10L, RepairStatus.COMPLETED)).thenReturn(true);
        RepairWork work = mock(RepairWork.class);
        when(work.getId()).thenReturn(20L);
        when(repairWorkRepository.findByRepair_Id(10L)).thenReturn(List.of(work));

        Part motor = part(1L, "모터", 1000, 10, 5, PartStatus.LOW_QUANTITY);
        RepairPartUsage motorUsage = usage(motor, 1);
        when(repairPartUsageRepository.findByRepairWorkIdIn(List.of(20L)))
                .thenReturn(List.of(motorUsage));
        when(partRepository.findByIdAndStatus(1L, PartStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partService.updateMultiStockQuantity(user(7L), 10L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않거나 사용 불가능한 부품입니다.");
        assertThat(motor.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("사용 수량이 재고보다 많으면 재고 일괄 반영을 중단한다")
    void updateMultiStockQuantity_insufficient() {
        when(repairRepository.existsByIdAndStatus(10L, RepairStatus.COMPLETED)).thenReturn(true);
        RepairWork work = mock(RepairWork.class);
        when(work.getId()).thenReturn(20L);
        when(repairWorkRepository.findByRepair_Id(10L)).thenReturn(List.of(work));

        Part motor = part(1L, "모터", 1000, 10, 1, PartStatus.ACTIVE);
        RepairPartUsage motorUsage = usage(motor, 2);
        when(repairPartUsageRepository.findByRepairWorkIdIn(List.of(20L)))
                .thenReturn(List.of(motorUsage));
        when(partRepository.findByIdAndStatus(1L, PartStatus.ACTIVE)).thenReturn(Optional.of(motor));

        assertThatThrownBy(() -> partService.updateMultiStockQuantity(user(7L), 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
        assertThat(motor.getStockQuantity()).isEqualTo(1);
        verify(partStockHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Part part(Long id, String name, int unitPrice, int standardMinutes, int stock, PartStatus status) {
        Part part = new Part(name, unitPrice, standardMinutes, stock);
        ReflectionTestUtils.setField(part, "id", id);
        ReflectionTestUtils.setField(part, "status", status);
        return part;
    }

    private CustomUserDetails user(Long id) {
        CustomUserDetails user = mock(CustomUserDetails.class);
        lenient().when(user.getId()).thenReturn(id);
        return user;
    }

    private RepairPartUsage usage(Part part, int quantity) {
        RepairPartUsage usage = mock(RepairPartUsage.class);
        when(usage.getPart()).thenReturn(part);
        lenient().when(usage.getQuantity()).thenReturn(quantity);
        return usage;
    }

    private PartStockHistory capturedHistory() {
        ArgumentCaptor<PartStockHistory> captor = ArgumentCaptor.forClass(PartStockHistory.class);
        verify(partStockHistoryRepository).save(captor.capture());
        return captor.getValue();
    }
}
