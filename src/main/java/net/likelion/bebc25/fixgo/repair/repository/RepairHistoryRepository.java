package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepairHistoryRepository extends JpaRepository<Repair, Long> {

    Optional<Repair> findByAsReceptionId(Long asReceptionId);
}