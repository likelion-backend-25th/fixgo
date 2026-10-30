package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairSymptomRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairSymptomRelationRepository extends JpaRepository<RepairSymptomRelation, Long> {

    // 수리 프리셋별 증상 관계 목록 조회
    @Query("SELECT rsr FROM RepairSymptomRelation rsr WHERE rsr.repairPreset.id = :repairPresetId")
    List<RepairSymptomRelation> findByRepairPresetId(@Param("repairPresetId") Long repairPresetId);
}