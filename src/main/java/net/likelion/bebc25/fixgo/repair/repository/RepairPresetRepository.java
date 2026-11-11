package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepairPresetRepository extends JpaRepository<RepairPreset, Long> {

    // 프리셋 이름으로 검색
    List<RepairPreset> findByNameContaining(String name);

    // 프리셋 이름으로 조회
    List<RepairPreset> findByName(String name);

    // 프리셋별 예상 작업시간 조회 (부품 표준 작업시간 × 수량)
    @Query("""
                SELECT rpp.repairPreset.id AS repairPresetId,
                       COALESCE(SUM(p.standardMinutes * rpp.quantity), 0) AS estimatedMinutes
                FROM RepairPresetPart rpp
                JOIN rpp.part p
                GROUP BY rpp.repairPreset.id
            """)
    List<RepairPresetEstimatedMinutes> findEstimatedMinutesForAllPresets();

    interface RepairPresetEstimatedMinutes {
        Long getRepairPresetId();

        Long getEstimatedMinutes();
    }
}