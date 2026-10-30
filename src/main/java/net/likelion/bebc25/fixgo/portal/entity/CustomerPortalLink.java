package net.likelion.bebc25.fixgo.portal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerPortalLink {

    private static final int TOKEN_HASH_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long asRequestId;

    @Column(nullable = false, unique = true, length = TOKEN_HASH_LENGTH)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}