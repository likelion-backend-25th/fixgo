package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    // A/S 접수 ID로 수리 정보 조회
    @Query("SELECT r FROM Repair r WHERE r.asRequestId = :asRequestId")
    Optional<Repair> findByAsRequestId(@Param("asRequestId") Long asRequestId);

    @Query("SELECT r FROM Repair r WHERE r.technicianId = :technicianId")
    List<Repair> findByTechnicianId(@Param("technicianId") Long technicianId);

    List<Repair> findByStatus(RepairStatus status);

    @Query("SELECT r FROM Repair r WHERE r.technicianId = :technicianId AND r.status = :status")
    List<Repair> findByTechnicianIdAndStatus(@Param("technicianId") Long technicianId, @Param("status") RepairStatus status);
}