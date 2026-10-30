package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepairPresetRepositoryTest {

    @Autowired
    private RepairPresetRepository repairPresetRepository;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        repairPresetRepository.save(RepairPreset.builder().name("메인보드 교체").description("메인보드 불량으로 인한 교체").createdAt(now).updatedAt(now).createdBy("test").updatedBy("test").build());

        repairPresetRepository.save(RepairPreset.builder().name("메모리 교체").description("메모리 불량으로 인한 교체").createdAt(now).updatedAt(now).createdBy("test").updatedBy("test").build());

        repairPresetRepository.save(RepairPreset.builder().name("메인보드 점검").description("메인보드 상태 점검").createdAt(now).updatedAt(now).createdBy("test").updatedBy("test").build());
    }

    @Test
    @DisplayName("프리셋 이름 일부로 검색")
    void findByNameContaining() {
        // when
        List<RepairPreset> result = repairPresetRepository.findByNameContaining("메인보드");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RepairPreset::getName).containsExactlyInAnyOrder("메인보드 교체", "메인보드 점검");
    }

    @Test
    @DisplayName("프리셋 이름으로 정확히 조회")
    void findByName() {
        // when
        List<RepairPreset> result = repairPresetRepository.findByName("메모리 교체");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("메모리 교체");
    }

    @Test
    @DisplayName("존재하지 않는 프리셋 이름 검색")
    void findByNameNotFound() {
        // when
        List<RepairPreset> result = repairPresetRepository.findByName("그래픽카드 교체");

        // then
        assertThat(result).isEmpty();
    }
}
