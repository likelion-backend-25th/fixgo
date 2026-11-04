package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPartUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairPartUsageRepository extends JpaRepository<RepairPartUsage, Long> {

    // 여러 작업에 연결된 실제 사용 부품 조회
    List<RepairPartUsage> findByRepairWorkIdIn(List<Long> repairWorkIds);
}
