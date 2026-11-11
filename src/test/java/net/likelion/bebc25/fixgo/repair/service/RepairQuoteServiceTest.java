package net.likelion.bebc25.fixgo.repair.service;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.repository.AsReceptionRepository;
import net.likelion.bebc25.fixgo.payment.entity.PaymentBillType;
import net.likelion.bebc25.fixgo.payment.service.PaymentBillService;
import net.likelion.bebc25.fixgo.repair.dto.repair.CustomerQuoteView;
import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.RepairPartUsage;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.exception.RepairErrorCode;
import net.likelion.bebc25.fixgo.repair.exception.RepairException;
import net.likelion.bebc25.fixgo.repair.repository.RepairPartUsageRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairRepository;
import net.likelion.bebc25.fixgo.repair.repository.RepairWorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("고객 견적 조회/승인/거부 테스트")
class RepairQuoteServiceTest {

    private static final Long RECEPTION_ID = 100L;
    private static final Long REPAIR_ID = 1L;

    @Mock
    private RepairRepository repairRepository;
    @Mock
    private RepairWorkRepository repairWorkRepository;
    @Mock
    private RepairPartUsageRepository repairPartUsageRepository;
    @Mock
    private AsReceptionRepository asReceptionRepository;
    @Mock
    private RepairStatusService repairStatusService;
    @Mock
    private RepairCostService repairCostService;
    @Mock
    private PaymentBillService paymentBillService;

    @Mock
    private Repair repair;
    @Mock
    private AsReception reception;

    private RepairQuoteService repairQuoteService;

    @BeforeEach
    void setUp() {
        repairQuoteService = new RepairQuoteService(
                repairRepository,
                repairWorkRepository,
                repairPartUsageRepository,
                asReceptionRepository,
                repairStatusService,
                repairCostService,
                paymentBillService
        );
    }

    @Test
    @DisplayName("수리비가 확정되지 않았으면 견적이 비어 있다.")
    void findCustomerViewBeforeConfirm() {
        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.of(repair));
        when(repair.getConfirmedAt()).thenReturn(null);

        assertThat(repairQuoteService.findCustomerView(RECEPTION_ID)).isEmpty();
    }

    @Test
    @DisplayName("수리 정보가 없으면 견적이 비어 있다.")
    void findCustomerViewWithoutRepair() {
        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.empty());

        assertThat(repairQuoteService.findCustomerView(RECEPTION_ID)).isEmpty();
    }

    @Test
    @DisplayName("확정된 견적은 부품 항목과 금액을 담아 승인 대기 상태로 보여 준다.")
    void findCustomerViewPending() {
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 11, 9, 10, 0);
        RepairWork work = mock(RepairWork.class);
        RepairPartUsage usage = mock(RepairPartUsage.class);
        Part part = mock(Part.class);

        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.of(repair));
        when(repair.getId()).thenReturn(REPAIR_ID);
        when(repair.getConfirmedAt()).thenReturn(confirmedAt);
        when(repair.getStatus()).thenReturn(RepairStatus.REQUESTED);
        when(repair.getDiagnosis()).thenReturn("키보드 연결부 불량");
        when(repair.getLaborRate()).thenReturn(5_000);
        when(repair.getFinalAmount()).thenReturn(85_000);
        when(work.getId()).thenReturn(7L);
        when(repairWorkRepository.findByRepair_Id(REPAIR_ID)).thenReturn(List.of(work));
        when(repairPartUsageRepository.findByRepairWorkIdIn(List.of(7L))).thenReturn(List.of(usage));
        when(usage.getPart()).thenReturn(part);
        when(part.getName()).thenReturn("키보드");
        when(usage.getQuantity()).thenReturn(2);
        when(usage.getUnitPrice()).thenReturn(40_000);

        CustomerQuoteView view = repairQuoteService.findCustomerView(RECEPTION_ID).orElseThrow();

        assertThat(view.status()).isEqualTo(CustomerQuoteView.Status.PENDING);
        assertThat(view.diagnosis()).isEqualTo("키보드 연결부 불량");
        assertThat(view.items()).containsExactly(new CustomerQuoteView.Item("키보드", 2, 40_000, 80_000));
        assertThat(view.laborCost()).isEqualTo(5_000);
        assertThat(view.totalAmount()).isEqualTo(85_000);
        assertThat(view.issuedAt()).isEqualTo(confirmedAt);
    }

    @Test
    @DisplayName("수리가 진행 중이어도 견적은 승인된 상태로 보인다.")
    void findCustomerViewApprovedWhileRepairing() {
        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.of(repair));
        when(repair.getConfirmedAt()).thenReturn(LocalDateTime.now());
        when(repair.getStatus()).thenReturn(RepairStatus.REPAIRING);
        when(repairWorkRepository.findByRepair_Id(any())).thenReturn(List.of());

        CustomerQuoteView view = repairQuoteService.findCustomerView(RECEPTION_ID).orElseThrow();

        assertThat(view.status()).isEqualTo(CustomerQuoteView.Status.APPROVED);
        assertThat(view.items()).isEmpty();
    }

    @Test
    @DisplayName("견적을 승인하면 수리 상태를 승인으로 바꾼다.")
    void approve() {
        preparePendingQuote();

        repairQuoteService.approveByCustomer(RECEPTION_ID);

        verify(repairStatusService).changeStatus(repair, RepairStatus.APPROVED, null, "고객 견적 승인");
        verifyNoInteractions(paymentBillService);
    }

    @Test
    @DisplayName("견적을 거부하면 수리 상태를 거부로 바꾸고 점검비 청구서를 발행한다.")
    void reject() {
        preparePendingQuote();
        when(repairCostService.getInspectionCost(repair)).thenReturn(15_000);

        repairQuoteService.rejectByCustomer(RECEPTION_ID);

        verify(repairStatusService).changeStatus(repair, RepairStatus.REJECTED, null, "고객 견적 거부");
        verify(paymentBillService).createBill(RECEPTION_ID, 15_000L, PaymentBillType.INSPECTION);
    }

    @Test
    @DisplayName("이미 승인된 견적은 다시 승인하거나 거부할 수 없다.")
    void rejectNotPending() {
        when(asReceptionRepository.findByIdWithLock(RECEPTION_ID)).thenReturn(Optional.of(reception));
        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.of(repair));
        when(reception.getStatus()).thenReturn(AsReceptionStatus.AWAITING_REPAIR);

        assertThatThrownBy(() -> repairQuoteService.rejectByCustomer(RECEPTION_ID))
                .isInstanceOf(RepairException.class)
                .extracting("errorCode")
                .isEqualTo(RepairErrorCode.QUOTE_NOT_PENDING);

        verifyNoInteractions(repairStatusService, paymentBillService);
    }

    @Test
    @DisplayName("수리 정보가 없으면 승인할 수 없다.")
    void approveWithoutRepair() {
        when(asReceptionRepository.findByIdWithLock(RECEPTION_ID)).thenReturn(Optional.of(reception));
        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairQuoteService.approveByCustomer(RECEPTION_ID))
                .isInstanceOf(RepairException.class);

        verifyNoInteractions(repairStatusService);
    }

    private void preparePendingQuote() {
        when(asReceptionRepository.findByIdWithLock(RECEPTION_ID)).thenReturn(Optional.of(reception));
        when(repairRepository.findByAsReception_Id(RECEPTION_ID)).thenReturn(Optional.of(repair));
        when(reception.getStatus()).thenReturn(AsReceptionStatus.AWAITING_QUOTE_APPROVAL);
        when(repair.getStatus()).thenReturn(RepairStatus.REQUESTED);
    }
}
