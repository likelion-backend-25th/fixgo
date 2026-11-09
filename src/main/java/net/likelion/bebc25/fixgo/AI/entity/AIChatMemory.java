package net.likelion.bebc25.fixgo.AI.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "IDX_CONVERSATION_ID_TIMESTAMP", columnList = "conversation_id, timestamp")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIChatMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String conversationId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
