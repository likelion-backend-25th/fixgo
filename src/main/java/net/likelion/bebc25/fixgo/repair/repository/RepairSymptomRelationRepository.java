package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import net.likelion.bebc25.fixgo.repair.entity.RepairSymptomRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairSymptomRelationRepository extends JpaRepository<RepairSymptomRelation, Long> {

    // 수리 프리셋별 증상 관계 목록 조회
    @Query("SELECT rsr FROM RepairSymptomRelation rsr WHERE rsr.repairPreset.id = :repairPresetId")
    List<RepairSymptomRelation> findByRepairPresetId(@Param("repairPresetId") Long repairPresetId);

    // 증상에 연결된 수리 프리셋 목록 조회 (모달 2열)
    @Query("""
            SELECT rsr.repairPreset FROM RepairSymptomRelation rsr
            WHERE rsr.symptomPreset.id = :symptomId
            ORDER BY rsr.repairPreset.id DESC
            """)
    List<RepairPreset> findRepairPresetsBySymptomId(@Param("symptomId") Long symptomId);

    // 수리 프리셋에 연결된 증상 이름 목록 (프리셋 목록의 "증상" 컬럼)
    @Query("""
            SELECT rsr.symptomPreset.name FROM RepairSymptomRelation rsr
            WHERE rsr.repairPreset.id = :repairPresetId
            """)
    List<String> findSymptomNamesByRepairPresetId(@Param("repairPresetId") Long repairPresetId);
}