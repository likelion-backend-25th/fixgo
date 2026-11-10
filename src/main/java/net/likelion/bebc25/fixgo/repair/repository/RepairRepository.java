package net.likelion.bebc25.fixgo.repair.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    // A/S 접수 ID로 수리 정보 조회
    Optional<Repair> findByAsReception_Id(Long asReceptionId);

    // 담당 기사 ID로 수리 목록 조회
    List<Repair> findByAsReception_Technician_Id(Long technicianId);

    // 수리 상태로 수리 목록 조회
    List<Repair> findByStatus(RepairStatus status);

    // 담당 기사 ID와 수리 상태로 수리 목록 조회
    List<Repair> findByTechnician_IdAndStatus(
            Long technicianId,
            RepairStatus status
    );

    // 담당 기사 ID와 특정 상태가 아닌 수리 목록 조회
    List<Repair> findByTechnician_IdAndStatusNot(
            Long technicianId,
            RepairStatus status
    );

    // A/S 접수번호로 수리 정보 조회
    Optional<Repair> findByAsReception_ReceptionNumber(String receptionNumber);

    boolean existsByIdAndStatus(Long repairId, RepairStatus status);

    // 수리 ID와 담당 기사 ID로 수리 정보 조회
    Optional<Repair> findByIdAndAsReception_Technician_Id(Long id, Long technicianId);

    // 수리 정보 비관적 쓰기 잠금 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Repair r WHERE r.id = :id")
    Optional<Repair> findByIdWithLock(@Param("id") Long id);

    // 수리 목록 조회 시 연관 엔티티 Fetch Join
    @Query("""
            SELECT r
            FROM Repair r
            JOIN FETCH r.asReception
            JOIN FETCH r.technician
            """)
    List<Repair> findAllWithDetails();
}
