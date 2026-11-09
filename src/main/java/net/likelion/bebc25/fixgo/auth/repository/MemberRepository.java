package net.likelion.bebc25.fixgo.auth.repository;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Member> findAllByOrderByCreatedAtDesc();

    List<Member> findAllByStatusNotOrderByCreatedAtDesc(Status status);

    @Query("""
                SELECT m
                FROM Member m
                WHERE (
                    :keyword IS NULL
                    OR m.name LIKE CONCAT('%', :keyword, '%')
                    OR m.email LIKE CONCAT('%', :keyword, '%')
                )
                AND (:role IS NULL OR m.role = :role)
                AND m.status <> :deleted
                AND (:status IS NULL OR m.status = :status)
                ORDER BY m.createdAt DESC
            """)
    Page<Member> searchMembers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("status") Status status,
            @Param("deleted") Status deleted,
            Pageable pageable
    );

    Page<Member> searchMembersByStatus(Status status, Pageable pageable);

    String ASSIGNABLE_TECHNICIANS_FILTER = """
            WHERE m.role = 'ROLE_TECHNICIAN'
              AND m.status = 'ACTIVE'
              AND LOCATE(LOWER(:keyword), LOWER(m.name)) > 0
            """;

    @Query(value = """
            SELECT candidates.*
            FROM (
                SELECT m.id AS technicianId, m.name, m.email,
                    (SELECT COUNT(*)
                     FROM as_reception ar
                     WHERE ar.technician_id = m.id AND ar.status = 'RECEIVED'
                       AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id)
                    ) AS inspectionWaitingCount,
                    (SELECT COUNT(*)
                     FROM repair r
                     WHERE r.technician_id = m.id AND r.status = 'REPAIRING'
                    ) + (SELECT COUNT(*)
                         FROM as_reception ar
                         WHERE ar.technician_id = m.id AND ar.status = 'REPAIR_IN_PROGRESS'
                           AND NOT EXISTS (SELECT 1 FROM repair r WHERE r.as_reception_id = ar.id)
                    ) AS repairInProgressCount,
                    (SELECT AVG(TIMESTAMPDIFF(SECOND,
                        (SELECT MAX(started.created_at)
                         FROM as_status_history started
                         WHERE started.as_reception_id = finished.as_reception_id
                           AND started.to_status = 'REPAIRING'
                           AND started.id < finished.id
                           AND started.created_at <= finished.created_at),
                        finished.created_at)) / 60.0
                     FROM repair r
                     JOIN as_status_history finished ON finished.as_reception_id = r.as_reception_id
                     WHERE r.technician_id = m.id
                       AND finished.from_status = 'REPAIRING' AND finished.to_status = 'COMPLETED'
                    ) AS estimatedDurationMinutes
                FROM member m
            """ + ASSIGNABLE_TECHNICIANS_FILTER + """
            ) candidates
            ORDER BY
                CASE WHEN :sortBy = 'NAME' AND :direction = 'ASC' THEN candidates.name END ASC,
                CASE WHEN :sortBy = 'NAME' AND :direction = 'DESC' THEN candidates.name END DESC,
                CASE WHEN :sortBy = 'ESTIMATED_DURATION_MINUTES'
                     AND candidates.estimatedDurationMinutes IS NULL THEN 1 ELSE 0 END,
                CASE WHEN :direction = 'DESC' THEN -1 ELSE 1 END *
                    CASE :sortBy
                        WHEN 'INSPECTION_WAITING_COUNT' THEN candidates.inspectionWaitingCount
                        WHEN 'REPAIR_IN_PROGRESS_COUNT' THEN candidates.repairInProgressCount
                        WHEN 'ESTIMATED_DURATION_MINUTES' THEN candidates.estimatedDurationMinutes
                    END ASC,
                candidates.technicianId ASC
            """, countQuery = "SELECT COUNT(*) FROM member m " + ASSIGNABLE_TECHNICIANS_FILTER, nativeQuery = true)
    Page<TechnicianCandidate> findAssignableTechnicians(
            @Param("keyword") String keyword,
            @Param("sortBy") String sortBy,
            @Param("direction") String direction,
            Pageable pageable
    );

    interface TechnicianCandidate {
        Long getTechnicianId();

        String getName();

        String getEmail();

        long getInspectionWaitingCount();

        long getRepairInProgressCount();

        Double getEstimatedDurationMinutes();
    }

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndStatusNot(String email, Status status);

    Optional<Member> findByIdAndStatusNot(Long id, Status status);
}
