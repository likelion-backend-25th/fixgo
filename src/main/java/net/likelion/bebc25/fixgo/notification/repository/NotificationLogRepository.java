package net.likelion.bebc25.fixgo.notification.repository;

import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByEventKey(String eventKey);

    boolean existsByEventKey(String eventKey);
}
