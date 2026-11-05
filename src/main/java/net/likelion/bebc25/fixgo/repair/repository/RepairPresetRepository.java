package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RepairPresetRepository extends JpaRepository<RepairPreset, Long> {

    // 프리셋 이름으로 검색
    List<RepairPreset> findByNameContaining(String name);

    // 프리셋 이름으로 조회
    List<RepairPreset> findByName(String name);

    // 프리셋 정보 수정
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE RepairPreset rp
                SET rp.name = :name,
                    rp.description = :description,
                    rp.updatedAt = :updatedAt,
                    rp.updatedBy = :updatedBy
                WHERE rp.id = :id
            """)
    int updatePreset(@Param("id") Long id, @Param("name") String name, @Param("description") String description, @Param("updatedAt") LocalDateTime updatedAt, @Param("updatedBy") String updatedBy);
}