package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairSymptomPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairSymptomPresetRepository extends JpaRepository<RepairSymptomPreset, Long> {

    // A/S 수리 건별 증상 프리셋 목록 조회
    @Query("SELECT rsp FROM RepairSymptomPreset rsp WHERE rsp.repair.id = :repairId")
    List<RepairSymptomPreset> findByRepairId(@Param("repairId") Long repairId);

    // 증상 프리셋 이름으로 검색
    @Query("SELECT rsp FROM RepairSymptomPreset rsp WHERE rsp.symptomPresetNam LIKE CONCAT('%', :name, '%')")
    List<RepairSymptomPreset> findBySymptomPresetName(@Param("name") String name);
}