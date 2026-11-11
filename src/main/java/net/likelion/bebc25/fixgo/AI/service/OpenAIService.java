package net.likelion.bebc25.fixgo.AI.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.AI.entity.Chat;
import net.likelion.bebc25.fixgo.AI.repository.ChatRepository;
import net.likelion.bebc25.fixgo.AI.tools.StatisticsTools;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OpenAIService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    private static final String SYSTEM_PROMPT = """
            너는 FixGo 관리자용 통계 조회 도우미야.
            오늘 날짜는 {today} 이다. 이전 대화나 모델이 기억한 날짜가 달라도 이 날짜만 오늘로 사용한다.
            통계 관련 질문이면 반드시 제공된 Tool을 호출해서 실제 데이터를 가져와.
            Tool 결과에 없는 숫자는 추측하지 마.
            '오늘', '이번 달', '올해'처럼 날짜가 애매하면 {today} 기준으로 해석하고, yyyy-MM-dd로 Tool에 넘겨.

            Tool 결과를 사용자에게 보여줄 때 아래 규칙을 지켜.
            1. 조회 기간(시작일~종료일)을 먼저 밝혀.
            2. 수치는 그대로 쓰고, 건수/원/% 단위를 붙여.
            3. 추측하거나 없는 지표를 만들지 마.
            4. 가능하면 짧은 요약 + 핵심 수치 목록으로 답해.
            """;

    private String systemPrompt() {
        return SYSTEM_PROMPT.replace("{today}", LocalDate.now(BUSINESS_ZONE).toString());
    }

    private final OpenAiChatModel openAiChatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;
    private final StatisticsTools statisticsTools;

    /**
     * 비스트리밍 응답. generateStream과 동일한 Tool/system prompt 경로로
     * 통계 조회 결과를 한 번에 검증할 때 사용한다.
     */
    @Transactional
    public String generate(CustomUserDetails userDetails, String text) {
        String memberId = conversationId(userDetails);
        ChatMemory chatMemory = prepareConversation(memberId, text);

        String answer = chatClient()
                .prompt(buildPrompt(chatMemory, userId))
                .system(systemPrompt())
                .tools(statisticsTools.all())
                .call()
                .content();

        persistAssistantReply(memberId, chatMemory, answer);
        return answer;
    }

    @Transactional
    public Flux<String> generateStream(CustomUserDetails userDetails, String text) {
        String memberId = conversationId(userDetails);
        ChatMemory chatMemory = prepareConversation(memberId, text);
        StringBuilder responseBuffer = new StringBuilder();

        return chatClient()
                .prompt(buildPrompt(chatMemory, userId))
                .system(systemPrompt())
                .tools(statisticsTools.all())
                .stream()
                .content()
                .map(token -> {
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> persistAssistantReply(memberId, chatMemory, responseBuffer.toString()));
    }

    private ChatClient chatClient() {
        return ChatClient.create(openAiChatModel);
    }

    private String conversationId(CustomUserDetails userDetails) {
        return userDetails.getId() + "";
    }

    private ChatMemory prepareConversation(String memberId, String text) {
        chatRepository.save(new Chat(memberId, text, MessageType.USER));

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(memberId, new UserMessage(text));
        return chatMemory;
    }

    private Prompt buildPrompt(ChatMemory chatMemory, String memberId) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();
        return new Prompt(chatMemory.get(memberId), options);
    }

    private void persistAssistantReply(String memberId, ChatMemory chatMemory, String answer) {
        chatMemory.add(memberId, new AssistantMessage(answer));
        chatMemoryRepository.saveAll(memberId, chatMemory.get(memberId));
        chatRepository.save(new Chat(memberId, answer, MessageType.ASSISTANT));
    }
}
