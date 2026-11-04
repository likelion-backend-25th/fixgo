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
    @Query("SELECT r FROM Repair r WHERE r.asReception.id = :asRequestId")
    Optional<Repair> findByAsReceptionId(@Param("asRequestId") Long asRequestId);

    // 담당 기사 ID로 수리 목록 조회
    @Query("SELECT r FROM Repair r WHERE r.technician.id = :technicianId")
    List<Repair> findByTechnicianId(@Param("technicianId") Long technicianId);

    // 수리 상태로 수리 목록 조회
    List<Repair> findByStatus(RepairStatus status);

    // 담당 기사 ID와 수리 상태로 수리 목록 조회
    @Query("""
            SELECT r FROM Repair r
            WHERE r.technician.id = :technicianId
            AND r.status = :status
            """)
    List<Repair> findByTechnicianIDAndStatus(@Param("technicianId") Long technicianId, @Param("status") RepairStatus status);


    List<Repair> findByTechnicianIdAndStatusNot(Long technicianId, RepairStatus status);
}
