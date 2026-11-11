package net.likelion.bebc25.fixgo.AI.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Spring AI JDBC Chat Memory 테이블의 복합키.
 * conversation_id + sequence_id 조합으로 메시지 순서를 유일하게 식별한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SpringAiChatMemoryId implements Serializable {

    private String conversationId;
    private Long sequenceId;
}
