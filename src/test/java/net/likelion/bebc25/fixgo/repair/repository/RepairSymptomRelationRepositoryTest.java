package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import net.likelion.bebc25.fixgo.repair.entity.RepairSymptomRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepairSymptomRelationRepositoryTest {

    @Autowired
    private RepairSymptomRelationRepository repairSymptomRelationRepository;

    @Autowired
    private RepairPresetRepository repairPresetRepository;

    private RepairPreset repairPreset;

    @BeforeEach
    void setUp() {

        LocalDateTime now = LocalDateTime.now();

        // 테스트용 수리 프리셋 생성
        repairPreset = new RepairPreset("부품 교체", "부품 교체 작업 프리셋", now, now, "tester", "tester");

        repairPreset = repairPresetRepository.save(repairPreset);

        // 테스트용 증상 관계 데이터 생성
        RepairSymptomRelation relation1 = new RepairSymptomRelation(repairPreset, now);

        RepairSymptomRelation relation2 = new RepairSymptomRelation(repairPreset, now);

        RepairSymptomRelation relation3 = new RepairSymptomRelation(repairPreset, now);

        repairSymptomRelationRepository.saveAll(List.of(relation1, relation2, relation3));
    }

    @Test
    @DisplayName("수리 프리셋별 증상 관계 목록 조회")
    void findByRepairPresetId() {

        // when
        List<RepairSymptomRelation> result = repairSymptomRelationRepository.findByRepairPresetId(repairPreset.getId());

        // then
        assertThat(result).hasSize(3);

        assertThat(result).allMatch(relation -> relation.getRepairPreset().getId().equals(repairPreset.getId()));
    }
}