package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairWorkRepository extends JpaRepository<RepairWork, Long> {

    // A/S 수리 건별 작업 내역 조회
    @Query("SELECT rw FROM RepairWork rw WHERE rw.repair.id = :repairId")
    List<RepairWork> findByRepairId(@Param("repairId") Long repairId);

    // 작업명으로 검색
    List<RepairWork> findByWorkNameContaining(String workName);
}