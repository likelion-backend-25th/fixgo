package net.likelion.bebc25.fixgo.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프리셋 이름
    @Column(nullable = false, length = 255)
    private String name;

    // 이메일 제목
    @Column(nullable = false, length = 255)
    private String messageTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 프리셋 생성
    public static EmailPreset create(String name, String messageTitle, String message) {
        EmailPreset emailPreset = new EmailPreset();

        emailPreset.name = name;
        emailPreset.messageTitle = messageTitle;
        emailPreset.message = message;

        return emailPreset;
    }

    public void update(String name, String messageTitle, String message) {
        this.name = name;
        this.messageTitle = messageTitle;
        this.message = message;
    }
}
