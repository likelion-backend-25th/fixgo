package net.likelion.bebc25.fixgo.AI.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.AI.entity.Chat;
import net.likelion.bebc25.fixgo.AI.repository.ChatRepository;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OpenAIService {
    private final OpenAiChatModel openAiChatModel;
    //        private final OpenAiEmbeddingModel openAiEmbeddingModel;
    //        private final OpenAiImageModel openAiImageModel;
    //    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    //    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public String generate(CustomUserDetails userDetails, String text) {
        String userId = userDetails.getId() + "_" + "1";

        Chat chatUserEntity = new Chat(userId, text, MessageType.USER);

        // 메시지
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(text));

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        // 요청 및 응답
        ChatResponse response = openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @Transactional
    public Flux<String> generateStream(CustomUserDetails userDetails, String text) {
        ChatClient chatClient = ChatClient.create(openAiChatModel);

        String userId = userDetails.getId() + "_" + "1";

        // 전체 대화 저장용
        Chat chatUserEntity = new Chat(userId, text, MessageType.USER);

        // 메시지
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(text));

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        StringBuilder responseBuffer = new StringBuilder();

        // 요청 및 응답
        return chatClient.prompt(prompt).stream()
                .content()
                .map(token -> {
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    // chatMemory 저장
                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

                    // 전체 대화 저장용
                    Chat chatAssistantEntity = new Chat(userId, responseBuffer.toString(), MessageType.ASSISTANT);
                });
    }
}
