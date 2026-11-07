package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairPresetPart;
import net.likelion.bebc25.fixgo.repair.entity.RepairPresetPartId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairPresetPartRepository
        extends JpaRepository<RepairPresetPart, RepairPresetPartId> {

    List<RepairPresetPart> findByRepairPreset_Id(Long repairPresetId);

    List<RepairPresetPart> findByPart_Id(Long partId);

    void deleteByRepairPreset_Id(Long repairPresetId);
}
