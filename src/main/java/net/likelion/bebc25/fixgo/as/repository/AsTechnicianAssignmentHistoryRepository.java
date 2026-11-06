package net.likelion.bebc25.fixgo.as.repository;

import net.likelion.bebc25.fixgo.as.entity.AsTechnicianAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsTechnicianAssignmentHistoryRepository extends JpaRepository<AsTechnicianAssignmentHistory, Long> {

    List<AsTechnicianAssignmentHistory> findByAsReception_IdOrderByCreatedAtDescIdDesc(Long receptionId);

}
