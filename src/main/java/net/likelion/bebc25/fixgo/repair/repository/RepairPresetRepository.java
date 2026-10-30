package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairPresetRepository extends JpaRepository<RepairPreset, Long> {

    // 프리셋 이름으로 검색
    List<RepairPreset> findByNameContaining(String name);

    // 프리셋 이름으로 조회
    List<RepairPreset> findByName(String name);
}
