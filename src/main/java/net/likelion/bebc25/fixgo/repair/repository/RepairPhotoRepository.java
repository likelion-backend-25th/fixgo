package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairPhotoRepository extends JpaRepository<RepairPhoto, Long> {

    // A/S 수리 건별 사진 목록 조회
    @Query("SELECT rp FROM RepairPhoto rp WHERE rp.repair.id = :repairId")
    List<RepairPhoto> findByRepairId(@Param("repairId") Long repairId);

    // A/S 수리 건별 사진 유형 조회
    @Query("SELECT rp FROM RepairPhoto rp WHERE rp.repair.id = :repairId AND rp.photoType = :photoType")
    List<RepairPhoto> findByRepairIdAndPhotoType(@Param("repairId") Long repairId, @Param("photoType") String photoType);
}