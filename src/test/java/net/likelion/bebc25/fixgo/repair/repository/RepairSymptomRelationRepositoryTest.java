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
        repairPreset = new RepairPreset();
        repairPreset.setName("부품 교체");
        repairPreset.setDescription("부품 교체 작업 프리셋");
        repairPreset.setCreatedAt(now);
        repairPreset.setUpdatedAt(now);
        repairPreset.setCreatedBy("test");
        repairPreset.setUpdatedBy("test");

        repairPreset = repairPresetRepository.save(repairPreset);

        // 테스트용 증상 관계 데이터 생성
        RepairSymptomRelation relation1 = new RepairSymptomRelation();
        relation1.setRepairPreset(repairPreset);
        relation1.setCreatedAt(now);
        repairSymptomRelationRepository.save(relation1);

        RepairSymptomRelation relation2 = new RepairSymptomRelation();
        relation2.setRepairPreset(repairPreset);
        relation2.setCreatedAt(now);
        repairSymptomRelationRepository.save(relation2);

        RepairSymptomRelation relation3 = new RepairSymptomRelation();
        relation3.setRepairPreset(repairPreset);
        relation3.setCreatedAt(now);
        repairSymptomRelationRepository.save(relation3);
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