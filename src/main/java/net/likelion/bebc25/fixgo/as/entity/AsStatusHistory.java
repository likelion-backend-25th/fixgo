package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.common.entity.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {@Index(name = "IDX_ASH_REQUEST_TIME", columnList = "as_request_id, created_at")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AsStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "as_request_id", nullable = false)
    private AsRequest asRequest;

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

    public AsStatusHistory(AsRequest asRequest, String fromStatus, String toStatus, String reason, Member changedBy) {
        this.asRequest = asRequest;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }
}