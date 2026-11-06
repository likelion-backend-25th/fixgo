package net.likelion.bebc25.fixgo.as.entity;

import jakarta.persistence.*;
import lombok.Getter;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
public class AsTechnicianAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private AsReception asReception;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Member previousTechnician;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Member newTechnician;
    @Column(length = 1000)
    private String reason;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Member changedBy;

    protected AsTechnicianAssignmentHistory() {

    }

    public AsTechnicianAssignmentHistory(
            AsReception asReception,
            Member previousTechnician,
            Member newTechnician,
            String reason,
            Member changedBy
    ) {
        this.asReception = asReception;
        this.previousTechnician = previousTechnician;
        this.newTechnician = newTechnician;
        this.reason = reason;
        this.changedBy = changedBy;
    }

}
