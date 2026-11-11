package net.likelion.bebc25.fixgo.AI.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;

/**
 * Spring AI {@code JdbcChatMemoryRepository}가 사용하는 SPRING_AI_CHAT_MEMORY 테이블 매핑.
 * <p>
 * 실제 읽고 쓰기는 JDBC Repository가 담당하고, 이 엔티티는 Hibernate ddl-auto로
 * 스키마를 생성하기 위한 용도이다. (기존 schema.sql 대체)
 * <p>
 * 백틱으로 감싸야 한다. 기본 이름 전략은 인용되지 않은 이름을 소문자로 바꾸고,
 * RDS는 대소문자를 구분해서 {@code SPRING_AI_CHAT_MEMORY}를 찾지 못한다.
 */
@Entity
@Table(
        name = "`SPRING_AI_CHAT_MEMORY`",
        indexes = {
                @Index(
                        name = "SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX",
                        columnList = "conversation_id, timestamp"
                ),
                @Index(
                        name = "SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX",
                        columnList = "conversation_id, sequence_id"
                )
        }
)
@IdClass(SpringAiChatMemoryId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpringAiChatMemory {

    @Id
    @Column(name = "conversation_id", length = 36, nullable = false)
    private String conversationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "ENUM('USER','ASSISTANT','SYSTEM','TOOL')"
    )
    private MessageType type;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Id
    @Column(name = "sequence_id", nullable = false)
    private Long sequenceId;

    public SpringAiChatMemory(
            String conversationId,
            String content,
            MessageType type,
            LocalDateTime timestamp,
            Long sequenceId
    ) {
        this.conversationId = conversationId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.sequenceId = sequenceId;
    }
}
