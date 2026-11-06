package net.likelion.bebc25.fixgo.repair.entity.repair;

import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RepairTest {

    @Test
    @DisplayName("수리 엔티티를 생성할 수 있다.")
    void createRepair() {
        // given
        AsReception asReception = mock(AsReception.class);
        Member technician = mock(Member.class);

        // when
        Repair repair = new Repair(
                asReception,
                technician,
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        // then
        assertThat(repair.getAsReception()).isEqualTo(asReception);
        assertThat(repair.getTechnician()).isEqualTo(technician);
        assertThat(repair.getWarrantyResult()).isEqualTo(WarrantyResult.FREE);
        assertThat(repair.getStatus()).isEqualTo(RepairStatus.REQUESTED);

        assertThat(repair.getDiagnosis()).isNull();
        assertThat(repair.getWorkNote()).isNull();
        assertThat(repair.getRepairDecision()).isNull();
        assertThat(repair.getConfirmedAt()).isNull();
        assertThat(repair.getLaborRate()).isZero();
        assertThat(repair.getInspectionCost()).isZero();
        assertThat(repair.getFinalAmount()).isZero();
    }

    @Test
    @DisplayName("수리 정보를 수정할 수 있다.")
    void updateRepairInfo() {
        // given
        AsReception asReception = mock(AsReception.class);
        Member technician = mock(Member.class);

        Repair repair = new Repair(
                asReception,
                technician,
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        AsReception updatedReception = mock(AsReception.class);
        Member updatedTechnician = mock(Member.class);

        // when
        repair.updateRepairInfo(
                updatedReception,
                updatedTechnician,
                "냉장고 냉각 불량 진단",
                "냉각 부품 교체 완료",
                RepairDecision.REPAIRABLE,
                WarrantyResult.PAID,
                "고객 요청에 따른 수리",
                20000,
                10000,
                50000,
                null
        );

        // then
        assertThat(repair.getAsReception()).isEqualTo(updatedReception);
        assertThat(repair.getTechnician()).isEqualTo(updatedTechnician);
        assertThat(repair.getDiagnosis()).isEqualTo("냉장고 냉각 불량 진단");
        assertThat(repair.getWorkNote()).isEqualTo("냉각 부품 교체 완료");
        assertThat(repair.getRepairDecision()).isEqualTo(RepairDecision.REPAIRABLE);
        assertThat(repair.getWarrantyResult()).isEqualTo(WarrantyResult.PAID);
        assertThat(repair.getReason()).isEqualTo("고객 요청에 따른 수리");
        assertThat(repair.getLaborRate()).isEqualTo(20000);
        assertThat(repair.getInspectionCost()).isEqualTo(10000);
        assertThat(repair.getFinalAmount()).isEqualTo(50000);
        assertThat(repair.getReasonForRejection()).isNull();
    }

    @Test
    @DisplayName("수리 상태를 변경할 수 있다.")
    void changeStatus() {
        // given
        Repair repair = new Repair(
                mock(AsReception.class),
                mock(Member.class),
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        // when
        repair.changeStatus(RepairStatus.REPAIRING);

        // then
        assertThat(repair.getStatus()).isEqualTo(RepairStatus.REPAIRING);
    }

    @Test
    @DisplayName("수리비를 최초 확정하면 확정 시간이 기록된다.")
    void confirmRepairCost() {
        // given
        Repair repair = new Repair(
                mock(AsReception.class),
                mock(Member.class),
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        // when
        repair.confirmRepairCost();

        // then
        assertThat(repair.getConfirmedAt()).isNotNull();
        assertThat(repair.getConfirmedAt())
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("이미 확정된 수리비를 다시 확정하면 예외가 발생한다.")
    void confirmRepairCostAlreadyConfirmed() {
        // given
        Repair repair = new Repair(
                mock(AsReception.class),
                mock(Member.class),
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        repair.confirmRepairCost();

        LocalDateTime firstConfirmedAt = repair.getConfirmedAt();

        // when & then
        assertThatThrownBy(repair::confirmRepairCost)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 수리비가 확정되었습니다.");

        assertThat(repair.getConfirmedAt()).isEqualTo(firstConfirmedAt);
    }

    @Test
    @DisplayName("수리비를 수정할 수 있다.")
    void updateRepairCost() {
        // given
        Repair repair = new Repair(
                mock(AsReception.class),
                mock(Member.class),
                WarrantyResult.FREE,
                RepairStatus.REQUESTED
        );

        // when
        repair.updateRepairCost(
                20000,
                10000,
                50000
        );

        // then
        assertThat(repair.getLaborRate()).isEqualTo(20000);
        assertThat(repair.getInspectionCost()).isEqualTo(10000);
        assertThat(repair.getFinalAmount()).isEqualTo(50000);
    }
}