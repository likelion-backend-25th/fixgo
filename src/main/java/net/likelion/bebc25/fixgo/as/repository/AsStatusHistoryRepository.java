package net.likelion.bebc25.fixgo.as.repository;

import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsStatusHistoryRepository extends JpaRepository<AsStatusHistory, Long> {
    @Query("""
                SELECT h
                FROM AsStatusHistory h
                WHERE h.asReception.id = :receptionId
                ORDER BY h.createdAt DESC
            """)
    List<AsStatusHistory> findHistoryByReceptionId(@Param("receptionId") Long receptionId);
}
