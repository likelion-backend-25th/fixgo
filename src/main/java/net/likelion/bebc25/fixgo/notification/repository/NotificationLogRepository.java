package net.likelion.bebc25.fixgo.notification.repository;

import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByEventKey(String eventKey);

    boolean existsByEventKey(String eventKey);

    // 오늘 발송 건수
    @Query("SELECT COUNT (n) FROM NotificationLog n WHERE n.createdAt >= :start AND n.createdAt < :end")
    long countToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 이메일 발송 이력 중 상태별 건수
    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.status = :status AND n.createdAt >= :start AND n.createdAt < :end")
    long countTodayByStatus(@Param("status") NotificationStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 이메일 발송 조회, 검색(이메일, 고객명), 기간별 검색, 페이지네이션,
    @Query("""
                        SELECT n
                        FROM NotificationLog n
                        LEFT JOIN n.asReception a
                        LEFT JOIN a.customer c
                        WHERE(:keyword IS NULL
                            OR LOWER(n.recipientEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        AND (:start IS NULL OR n.createdAt >= :start)
                        AND (:end IS NULL OR n.createdAt < :end)
                        ORDER By n.createdAt DESC, n.id DESC
            """)
    Page<NotificationLog> searchEmails(@Param("keyword") String keyword, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
