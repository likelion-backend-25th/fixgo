package net.likelion.bebc25.fixgo.auth.repository;


import net.likelion.bebc25.fixgo.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByMemberIdAndRevokedAtIsNull(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM RefreshToken r
            WHERE r.revokedAt IS NOT NULL
               OR r.expiresAt < :now
            """)
    int deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
}
