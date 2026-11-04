package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.repair.entity.Part;
import net.likelion.bebc25.fixgo.repair.entity.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairCostServiceTest {

    @Mock
    private RepairRepository repairRepository;

    @Mock
    private RepairWorkRepository repairWorkRepository;

    @Mock
    private RepairPartUsageRepository repairPartUsageRepository;

    @Mock
    private LaborRateRepository laborRateRepository;

    @Mock
    private ProductModelRepository productModelRepository;

    @InjectMocks
    private RepairCostService repairCostService;

    @Test
    @DisplayName("수리 가능한 경우 공임비와 부품비를 합산한다.")
    void calculateRepairCostRepairable() {
        Long repairId = 1L;
        String updatedBy = "tester";
        Category category = Category.values()[0];

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);
        ProductModel productModel = mock(ProductModel.class);

        when(repairRepository.findById(repairId)).thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(1L);
        when(productModelRepository.findById(1L)).thenReturn(Optional.of(productModel));
        when(productModel.getCategory()).thenReturn(category);
        when(repair.getRepairDecision()).thenReturn(RepairDecision.REPAIRABLE);

        LaborRate laborRate = mock(LaborRate.class);

        when(laborRateRepository.findLatestRate(eq(category), eq(RateType.LABOR_RATE), any(), any(Pageable.class))).thenReturn(List.of(laborRate));

        when(laborRate.getCost()).thenReturn(20000);

        RepairWork repairWork = mock(RepairWork.class);
        when(repairWork.getId()).thenReturn(10L);
        when(repairWorkRepository.findByRepairId(repairId)).thenReturn(List.of(repairWork));

        Part part = mock(Part.class);
        when(part.getStandardMinutes()).thenReturn(20);

        RepairPartUsage usage = mock(RepairPartUsage.class);
        when(usage.getPart()).thenReturn(part);
        when(usage.getUnitPrice()).thenReturn(1000);
        when(usage.getQuantity()).thenReturn(2);

        when(repairPartUsageRepository.findByRepairWorkIdIn(List.of(10L))).thenReturn(List.of(usage));

        int result = repairCostService.calculateRepairCost(repairId, updatedBy);

        assertThat(result).isEqualTo(60000);

        verify(repair).updateRepairCost(20000, 0, 60000);
        verify(repair).updateAuditInfo(updatedBy);
    }

    @Test
    @DisplayName("수리 불가능한 경우 점검비만 청구한다.")
    void calculateRepairCostUnrepairable() {
        Long repairId = 1L;
        String updatedBy = "tester";
        Category category = Category.values()[0];

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);
        ProductModel productModel = mock(ProductModel.class);

        when(repairRepository.findById(repairId)).thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(1L);
        when(productModelRepository.findById(1L)).thenReturn(Optional.of(productModel));
        when(productModel.getCategory()).thenReturn(category);
        when(repair.getRepairDecision()).thenReturn(RepairDecision.UNREPAIRABLE);

        LaborRate inspectionRate = mock(LaborRate.class);

        when(laborRateRepository.findLatestRate(eq(category), eq(RateType.INSPECTION_COST), any(), any(Pageable.class))).thenReturn(List.of(inspectionRate));

        when(inspectionRate.getCost()).thenReturn(15000);

        int result = repairCostService.calculateRepairCost(repairId, updatedBy);

        assertThat(result).isEqualTo(15000);

        verify(repair).updateRepairCost(0, 15000, 15000);
        verify(repair).updateAuditInfo(updatedBy);

        verify(repairWorkRepository, never()).findByRepairId(any());
        verify(repairPartUsageRepository, never()).findByRepairWorkIdIn(any());
    }

    @Test
    @DisplayName("수리 정보가 존재하지 않으면 예외가 발생한다.")
    void calculateRepairCostNotFound() {
        Long repairId = 999L;

        when(repairRepository.findById(repairId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairCostService.calculateRepairCost(repairId, "tester")).isInstanceOf(IllegalArgumentException.class).hasMessage("수리 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("기사가 수리비를 확정하면 Repair와 AsReception 상태가 변경된다.")
    void confirmRepairCostSuccess() {
        Long repairId = 1L;
        Long technicianId = 10L;
        String username = "technician";

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member technician = mock(Member.class);

        when(userDetails.getMember()).thenReturn(technician);
        when(technician.getRole()).thenReturn(Role.ROLE_TECHNICIAN);
        when(userDetails.getId()).thenReturn(technicianId);
        when(userDetails.getUsername()).thenReturn(username);

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);

        when(repairRepository.findById(repairId)).thenReturn(Optional.of(repair));
        when(repair.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(technicianId);
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);
        when(repair.getAsReception()).thenReturn(asReception);

        repairCostService.confirmRepairCost(repairId, userDetails);

        verify(repair).confirmRepairCost(username);
        verify(repair).changeStatus(RepairStatus.REQUESTED, username);
        verify(asReception).changeStatus(AsReceptionStatus.AWAITING_QUOTE_APPROVAL);
    }

    @Test
    @DisplayName("점검 중이 아닌 수리는 수리비를 확정할 수 없다.")
    void confirmRepairCostInvalidStatus() {
        Long repairId = 1L;
        Long technicianId = 10L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member technician = mock(Member.class);

        when(userDetails.getMember()).thenReturn(technician);
        when(technician.getRole()).thenReturn(Role.ROLE_TECHNICIAN);
        when(userDetails.getId()).thenReturn(technicianId);

        Repair repair = mock(Repair.class);

        when(repairRepository.findById(repairId)).thenReturn(Optional.of(repair));
        when(repair.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(technicianId);
        when(repair.getStatus()).thenReturn(RepairStatus.REPAIRING);

        assertThatThrownBy(() -> repairCostService.confirmRepairCost(repairId, userDetails)).isInstanceOf(IllegalArgumentException.class).hasMessage("점검 중인 건만 수리비를 확정할 수 있습니다.");

        verify(repair, never()).confirmRepairCost(anyString());
        verify(repair, never()).changeStatus(any(), anyString());
    }

    @Test
    @DisplayName("이미 수리비가 확정된 경우 재확정할 수 없다.")
    void confirmRepairCostAlreadyConfirmed() {
        Long repairId = 1L;
        Long technicianId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member loginMember = mock(Member.class);
        Member technician = mock(Member.class);
        Repair repair = mock(Repair.class);

        when(userDetails.getMember()).thenReturn(loginMember);
        when(loginMember.getRole()).thenReturn(Role.ROLE_TECHNICIAN);
        when(userDetails.getId()).thenReturn(technicianId);

        when(repairRepository.findById(repairId)).thenReturn(Optional.of(repair));

        when(repair.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(technicianId);
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);
        when(repair.getConfirmedAt()).thenReturn(LocalDateTime.now());

        assertThatThrownBy(() -> repairCostService.confirmRepairCost(repairId, userDetails)).isInstanceOf(IllegalArgumentException.class).hasMessage("이미 수리비가 확정되었습니다.");

        verify(repair, never()).confirmRepairCost(anyString());
        verify(repair, never()).changeStatus(any(), anyString());
    }
}
