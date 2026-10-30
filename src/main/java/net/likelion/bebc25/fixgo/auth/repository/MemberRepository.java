package net.likelion.bebc25.fixgo.auth.repository;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Optional<Member> findByEmail(String email);

}
