package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairDecision;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.exception.RepairException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private RepairStatusService repairStatusService;

    @Mock
    private AsReceptionRepository asReceptionRepository;

    @InjectMocks
    private RepairCostService repairCostService;

    @Test
    @DisplayName("수리 가능한 경우 공임비와 부품비를 합산한다.")
    void calculateRepairCostRepairable() {
        Long repairId = 1L;
        Category category = Category.values()[0];

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);
        ProductModel productModel = mock(ProductModel.class);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(1L);
        when(productModelRepository.findById(1L))
                .thenReturn(Optional.of(productModel));
        when(productModel.getCategory()).thenReturn(category);
        when(repair.getRepairDecision()).thenReturn(RepairDecision.REPAIRABLE);

        LaborRate laborRate = mock(LaborRate.class);

        when(laborRateRepository.findLatestRate(
                eq(category),
                eq(RateType.LABOR_RATE),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of(laborRate));

        when(laborRate.getCost()).thenReturn(20000);

        RepairWork repairWork = mock(RepairWork.class);
        when(repairWork.getId()).thenReturn(10L);
        when(repairWorkRepository.findByRepairId(repairId))
                .thenReturn(List.of(repairWork));

        Part part = mock(Part.class);
        when(part.getStandardMinutes()).thenReturn(20);

        RepairPartUsage usage = mock(RepairPartUsage.class);
        when(usage.getPart()).thenReturn(part);
        when(usage.getUnitPrice()).thenReturn(1000);
        when(usage.getQuantity()).thenReturn(2);

        when(repairPartUsageRepository.findByRepairWorkIdIn(List.of(10L)))
                .thenReturn(List.of(usage));

        Long result = repairCostService.calculateRepairCost(repairId);

        assertThat(result).isEqualTo(82000L);

        verify(repair).updateRepairCost(80000, 0, 82000);
        verify(repairRepository).findByIdWithLock(repairId);
    }

    @Test
    @DisplayName("수리 불가능한 경우 점검비만 청구한다.")
    void calculateRepairCostUnrepairable() {
        Long repairId = 1L;
        Category category = Category.values()[0];

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);
        ProductModel productModel = mock(ProductModel.class);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(1L);
        when(productModelRepository.findById(1L))
                .thenReturn(Optional.of(productModel));
        when(productModel.getCategory()).thenReturn(category);
        when(repair.getRepairDecision()).thenReturn(RepairDecision.UNREPAIRABLE);

        LaborRate inspectionRate = mock(LaborRate.class);

        when(laborRateRepository.findLatestRate(
                eq(category),
                eq(RateType.INSPECTION_COST),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of(inspectionRate));

        when(inspectionRate.getCost()).thenReturn(15000);

        Long result = repairCostService.calculateRepairCost(repairId);

        assertThat(result).isEqualTo(15000L);

        verify(repair).updateRepairCost(0, 15000, 15000);

        verify(repairWorkRepository, never()).findByRepairId(any());
        verify(repairPartUsageRepository, never()).findByRepairWorkIdIn(any());
    }

    @Test
    @DisplayName("수리 정보가 존재하지 않으면 예외가 발생한다.")
    void calculateRepairCostNotFound() {
        Long repairId = 999L;

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairCostService.calculateRepairCost(repairId))
                .isInstanceOf(RepairException.class)
                .hasMessage("수리 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("제품 모델이 존재하지 않으면 예외가 발생한다.")
    void calculateRepairCostProductModelNotFound() {
        Long repairId = 1L;

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(100L);
        when(productModelRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairCostService.calculateRepairCost(repairId))
                .isInstanceOf(RepairException.class)
                .hasMessage("제품 모델을 찾을 수 없습니다.");

        verify(laborRateRepository, never())
                .findLatestRate(any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("공임비 정보가 존재하지 않으면 예외가 발생한다.")
    void calculateRepairCostLaborRateNotFound() {
        Long repairId = 1L;
        Category category = Category.values()[0];

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);
        ProductModel productModel = mock(ProductModel.class);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(1L);
        when(productModelRepository.findById(1L))
                .thenReturn(Optional.of(productModel));
        when(productModel.getCategory()).thenReturn(category);
        when(repair.getRepairDecision()).thenReturn(RepairDecision.REPAIRABLE);

        when(laborRateRepository.findLatestRate(
                eq(category),
                eq(RateType.LABOR_RATE),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());

        assertThatThrownBy(() -> repairCostService.calculateRepairCost(repairId))
                .isInstanceOf(RepairException.class);

        verify(repairWorkRepository, never()).findByRepairId(any());
    }

    @Test
    @DisplayName("점검비 정보가 존재하지 않으면 예외가 발생한다.")
    void calculateRepairCostInspectionRateNotFound() {
        Long repairId = 1L;
        Category category = Category.values()[0];

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);
        Product product = mock(Product.class);
        ProductModel productModel = mock(ProductModel.class);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getAsReception()).thenReturn(asReception);
        when(asReception.getProduct()).thenReturn(product);
        when(product.getProductModelId()).thenReturn(1L);
        when(productModelRepository.findById(1L))
                .thenReturn(Optional.of(productModel));
        when(productModel.getCategory()).thenReturn(category);
        when(repair.getRepairDecision()).thenReturn(RepairDecision.UNREPAIRABLE);

        when(laborRateRepository.findLatestRate(
                eq(category),
                eq(RateType.INSPECTION_COST),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());

        assertThatThrownBy(() -> repairCostService.calculateRepairCost(repairId))
                .isInstanceOf(RepairException.class);
    }

    @Test
    @DisplayName("기사가 수리비를 확정하면 Repair와 AsReception 상태 변경을 요청한다.")
    void confirmRepairCostSuccess() {
        Long repairId = 1L;
        Long technicianId = 10L;
        Long receptionId = 100L;
        String username = "technician";

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member technician = mock(Member.class);

        when(userDetails.getMember()).thenReturn(technician);
        when(technician.getRole()).thenReturn(Role.ROLE_TECHNICIAN);
        when(userDetails.getId()).thenReturn(technicianId);
        when(userDetails.getUsername()).thenReturn(username);

        Repair repair = mock(Repair.class);
        AsReception asReception = mock(AsReception.class);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(technicianId);
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);
        when(repair.getConfirmedAt()).thenReturn(null);
        when(repair.getAsReception()).thenReturn(asReception);

        when(asReception.getId()).thenReturn(receptionId);
        when(asReceptionRepository.findByIdWithLock(receptionId))
                .thenReturn(Optional.of(asReception));

        repairCostService.confirmRepairCost(repairId, userDetails);

        verify(repair).confirmRepairCost();

        verify(repairStatusService).changeStatus(
                repair,
                RepairStatus.REQUESTED,
                username
        );

        verify(asReception).changeStatus(
                AsReceptionStatus.AWAITING_QUOTE_APPROVAL
        );

        verify(repairRepository).findByIdWithLock(repairId);
        verify(asReceptionRepository).findByIdWithLock(receptionId);

        verify(repair, never()).changeStatus(any());
    }

    @Test
    @DisplayName("기사 권한이 없는 사용자는 수리비를 확정할 수 없다.")
    void confirmRepairCostTechnicianOnly() {
        Long repairId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member member = mock(Member.class);

        when(userDetails.getMember()).thenReturn(member);
        when(member.getRole()).thenReturn(Role.ROLE_RECEPTIONIST);

        assertThatThrownBy(() ->
                                   repairCostService.confirmRepairCost(repairId, userDetails)
        )
                .isInstanceOf(RepairException.class)
                .hasMessage("기사 권한이 필요합니다.");

        verifyNoInteractions(repairRepository);
        verifyNoInteractions(asReceptionRepository);
    }

    @Test
    @DisplayName("담당 기사가 아닌 기사는 수리비를 확정할 수 없다.")
    void confirmRepairCostNotAssignedTechnician() {
        Long repairId = 1L;
        Long loginTechnicianId = 10L;
        Long assignedTechnicianId = 20L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Member loginMember = mock(Member.class);
        Member assignedTechnician = mock(Member.class);
        Repair repair = mock(Repair.class);

        when(userDetails.getMember()).thenReturn(loginMember);
        when(loginMember.getRole()).thenReturn(Role.ROLE_TECHNICIAN);
        when(userDetails.getId()).thenReturn(loginTechnicianId);

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getTechnician()).thenReturn(assignedTechnician);
        when(assignedTechnician.getId()).thenReturn(assignedTechnicianId);

        assertThatThrownBy(() ->
                                   repairCostService.confirmRepairCost(repairId, userDetails)
        )
                .isInstanceOf(RepairException.class)
                .hasMessage("담당 기사만 수리비를 확정할 수 있습니다.");

        verify(repair, never()).confirmRepairCost();
        verify(repairStatusService, never())
                .changeStatus(any(), any(), any());
        verifyNoInteractions(asReceptionRepository);
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

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));
        when(repair.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(technicianId);
        when(repair.getStatus()).thenReturn(RepairStatus.REPAIRING);

        assertThatThrownBy(() ->
                                   repairCostService.confirmRepairCost(repairId, userDetails)
        )
                .isInstanceOf(RepairException.class)
                .hasMessage("점검 중인 건만 수리비를 확정할 수 있습니다.");

        verify(repair, never()).confirmRepairCost();
        verify(repair, never()).changeStatus(any());
        verifyNoInteractions(asReceptionRepository);
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

        when(repairRepository.findByIdWithLock(repairId))
                .thenReturn(Optional.of(repair));

        when(repair.getTechnician()).thenReturn(technician);
        when(technician.getId()).thenReturn(technicianId);
        when(repair.getStatus()).thenReturn(RepairStatus.DIAGNOSING);
        when(repair.getConfirmedAt()).thenReturn(LocalDateTime.now());

        assertThatThrownBy(() ->
                                   repairCostService.confirmRepairCost(repairId, userDetails)
        )
                .isInstanceOf(RepairException.class)
                .hasMessage("이미 수리비가 확정되었습니다.");

        verify(repair, never()).confirmRepairCost();
        verify(repair, never()).changeStatus(any());
        verifyNoInteractions(asReceptionRepository);
    }
}
