package net.likelion.bebc25.fixgo.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailPreset extends BaseTimeEntity {

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
