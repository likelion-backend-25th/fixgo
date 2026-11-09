package net.likelion.bebc25.fixgo.AI.repository;

import net.likelion.bebc25.fixgo.AI.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserIdOrderByCreatedAtAsc(String userId);
}
