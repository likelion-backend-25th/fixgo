package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {@Index(name = "IDX_ASH_RECEPTION_TIME", columnList = "as_reception_id, created_at")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "as_reception_id", nullable = false)
    private AsReception asReception;

    @Column
    private String fromStatus;

    @Column(nullable = false)
    private String toStatus;

    @Column(length = 1000)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Member changedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public AsStatusHistory(AsReception asReception, String fromStatus, String toStatus, String reason, Member changedBy) {
        this.asReception = asReception;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }
}