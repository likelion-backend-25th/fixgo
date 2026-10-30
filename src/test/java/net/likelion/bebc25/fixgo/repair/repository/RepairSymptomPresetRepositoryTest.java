package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.Repair;
import net.likelion.bebc25.fixgo.repair.entity.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.RepairSymptomPreset;
import net.likelion.bebc25.fixgo.repair.entity.WarrantyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepairSymptomPresetRepositoryTest {

    @Autowired
    private RepairSymptomPresetRepository repairSymptomPresetRepository;

    @Autowired
    private RepairRepository repairRepository;

    private Repair repair;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // 테스트용 A/S 수리 데이터 생성
        repair = new Repair();
        repair.setWarrantyResult(WarrantyResult.FREE);
        repair.setStatus(RepairStatus.REQUESTED);
        repair.setCreatedAt(now);
        repair.setUpdatedAt(now);
        repair.setCreatedBy("test");
        repair.setUpdatedBy("test");

        repair = repairRepository.save(repair);

        // 테스트용 증상 프리셋 데이터 생성
        RepairSymptomPreset preset1 = new RepairSymptomPreset();
        preset1.setRepair(repair);
        preset1.setSymptomPresetNam("전원 불량");
        repairSymptomPresetRepository.save(preset1);

        RepairSymptomPreset preset2 = new RepairSymptomPreset();
        preset2.setRepair(repair);
        preset2.setSymptomPresetNam("화면 출력 불량");
        repairSymptomPresetRepository.save(preset2);

        RepairSymptomPreset preset3 = new RepairSymptomPreset();
        preset3.setRepair(repair);
        preset3.setSymptomPresetNam("전원 버튼 작동 불량");
        repairSymptomPresetRepository.save(preset3);
    }

    @Test
    @DisplayName("A/S 수리 건별 증상 프리셋 조회")
    void findByRepairId() {
        // when
        List<RepairSymptomPreset> result = repairSymptomPresetRepository.findByRepairId(repair.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(RepairSymptomPreset::getSymptomPresetNam).containsExactlyInAnyOrder("전원 불량", "화면 출력 불량", "전원 버튼 작동 불량");
    }

    @Test
    @DisplayName("증상 프리셋 이름 일부로 검색")
    void findBySymptomPresetName() {
        // when
        List<RepairSymptomPreset> result = repairSymptomPresetRepository.findBySymptomPresetName("전원");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RepairSymptomPreset::getSymptomPresetNam).containsExactlyInAnyOrder("전원 불량", "전원 버튼 작동 불량");
    }

    @Test
    @DisplayName("존재하지 않는 증상 프리셋 검색")
    void findBySymptomPresetNameNotFound() {
        // when
        List<RepairSymptomPreset> result = repairSymptomPresetRepository.findBySymptomPresetName("그래픽카드");

        // then
        assertThat(result).isEmpty();
    }
}