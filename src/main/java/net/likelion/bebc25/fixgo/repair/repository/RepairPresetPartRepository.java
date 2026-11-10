package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPresetPart;
import net.likelion.bebc25.fixgo.repair.entity.RepairPresetPartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairPresetPartRepository
        extends JpaRepository<RepairPresetPart, RepairPresetPartId> {

    List<RepairPresetPart> findByRepairPreset_Id(Long repairPresetId);

    List<RepairPresetPart> findByPart_Id(Long partId);

    void deleteByRepairPreset_Id(Long repairPresetId);

    @Query("""
            select rpp from RepairPresetPart rpp
            join fetch rpp.part
            where rpp.repairPreset.id = :presetId
            """)
    List<RepairPresetPart> findWithPartByPresetId(@Param("presetId") Long presetId);
}
