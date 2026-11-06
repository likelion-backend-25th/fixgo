package net.likelion.bebc25.fixgo.repair.entity.repair;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RepairWorkTest {

    @Test
    @DisplayName("수리 작업 내역을 생성할 수 있다.")
    void createRepairWork() {
        // given
        Repair repair = mock(Repair.class);
        String repairPresetName = "기본 수리 프리셋";
        String workName = "냉각 부품 교체";

        // when
        RepairWork repairWork = new RepairWork(
                repair,
                repairPresetName,
                workName
        );

        // then
        assertThat(repairWork.getRepair()).isEqualTo(repair);
        assertThat(repairWork.getRepairPresetName()).isEqualTo(repairPresetName);
        assertThat(repairWork.getWorkName()).isEqualTo(workName);
    }

    @Test
    @DisplayName("수리 작업 내역을 수정할 수 있다.")
    void updateRepairWork() {
        // given
        Repair repair = mock(Repair.class);

        RepairWork repairWork = new RepairWork(
                repair,
                "기존 프리셋",
                "기존 작업 내용"
        );

        String updatedPresetName = "변경된 프리셋";
        String updatedWorkName = "변경된 작업 내용";

        // when
        repairWork.updateRepairWork(
                updatedPresetName,
                updatedWorkName
        );

        // then
        assertThat(repairWork.getRepairPresetName())
                .isEqualTo(updatedPresetName);
        assertThat(repairWork.getWorkName())
                .isEqualTo(updatedWorkName);
        assertThat(repairWork.getRepair()).isEqualTo(repair);
    }

    @Test
    @DisplayName("수리 작업 내역 수정 시 기존 값이 변경된다.")
    void updateRepairWorkReplacesExistingValues() {
        // given
        RepairWork repairWork = new RepairWork(
                mock(Repair.class),
                "기존 프리셋",
                "기존 작업 내용"
        );

        // when
        repairWork.updateRepairWork(
                "새로운 프리셋",
                "새로운 작업 내용"
        );

        // then
        assertThat(repairWork.getRepairPresetName())
                .isNotEqualTo("기존 프리셋")
                .isEqualTo("새로운 프리셋");

        assertThat(repairWork.getWorkName())
                .isNotEqualTo("기존 작업 내용")
                .isEqualTo("새로운 작업 내용");
    }
}