package net.likelion.bebc25.fixgo.auth.repository;

import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.auth.entity.Status;
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
                ORDER BY m.createdAt DESC
            """)
    List<Member> searchMembers(
            @Param("keyword") String keyword,
            @Param("role") Role role
    );


    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndStatusNot(String email, Status status);


}
