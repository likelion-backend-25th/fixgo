package net.likelion.bebc25.fixgo.AI.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OpenAIService {
    private final OpenAiChatModel openAiChatModel;
//    private final OpenAiEmbeddingModel openAiEmbeddingModel;
//        private final OpenAiImageModel openAiImageModel;
//    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
//    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;


    public String generate(String text) {

        // 메시지
        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(text);
        AssistantMessage assistantMessage = new AssistantMessage("");

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        // 요청 및 응답
        ChatResponse response = openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    public Flux<String> generateStream(String text) {

        // 메시지
        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(text);
        AssistantMessage assistantMessage = new AssistantMessage("");

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        // 요청 및 응답
        return openAiChatModel.stream(prompt)
                .mapNotNull(response -> response.getResult().getOutput().getText());
    }
}
