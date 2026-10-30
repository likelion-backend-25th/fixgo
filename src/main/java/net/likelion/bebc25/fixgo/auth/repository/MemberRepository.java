package net.likelion.bebc25.fixgo.auth.repository;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Role role);

    Optional<Member> findByEmail(String email);

}
