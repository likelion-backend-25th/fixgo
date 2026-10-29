package net.likelion.bebc25.fixgo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedBy
    @Column(updatable = false, nullable = false)
    String created_by;
    @LastModifiedBy
    @Column(nullable = false)
    String updated_by;
    @CreatedDate    // Entity가 최초 저장될 때 생성일시를 자동으로 주입
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate   // Entity가 등록/수정될 때 등록/수정일시를 자동으로 주입
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
