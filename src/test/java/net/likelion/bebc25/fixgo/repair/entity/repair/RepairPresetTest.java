package net.likelion.bebc25.fixgo.repair.entity.repair;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepairPresetTest {

    @Test
    @DisplayName("수리 프리셋을 생성할 수 있다.")
    void createRepairPreset() {
        // given
        String name = "기본 수리 프리셋";
        String description = "기본 수리 작업에 사용하는 프리셋";

        // when
        RepairPreset repairPreset = new RepairPreset(name, description);

        // then
        assertThat(repairPreset.getName()).isEqualTo(name);
        assertThat(repairPreset.getDescription()).isEqualTo(description);
        assertThat(repairPreset.getId()).isNull();
    }

    @Test
    @DisplayName("수리 프리셋 이름과 설명을 수정할 수 있다.")
    void updatePreset() {
        // given
        RepairPreset repairPreset = new RepairPreset(
                "기존 프리셋",
                "기존 설명"
        );

        String updatedName = "변경된 프리셋";
        String updatedDescription = "변경된 설명";

        // when
        repairPreset.updatePreset(updatedName, updatedDescription);

        // then
        assertThat(repairPreset.getName()).isEqualTo(updatedName);
        assertThat(repairPreset.getDescription()).isEqualTo(updatedDescription);
    }

    @Test
    @DisplayName("수리 프리셋 설명을 null로 수정할 수 있다.")
    void updatePresetDescriptionToNull() {
        // given
        RepairPreset repairPreset = new RepairPreset(
                "기본 프리셋",
                "기존 설명"
        );

        // when
        repairPreset.updatePreset("기본 프리셋", null);

        // then
        assertThat(repairPreset.getName()).isEqualTo("기본 프리셋");
        assertThat(repairPreset.getDescription()).isNull();
    }

    @Test
    @DisplayName("수리 프리셋 ID를 설정할 수 있다.")
    void setId() {
        // given
        RepairPreset repairPreset = new RepairPreset(
                "기본 프리셋",
                "기본 설명"
        );

        Long id = 1L;

        // when
        repairPreset.setId(id);

        // then
        assertThat(repairPreset.getId()).isEqualTo(id);
    }
}