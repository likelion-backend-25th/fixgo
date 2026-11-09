package net.likelion.bebc25.fixgo.AI.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.AI.dto.OpenAIDto;
import net.likelion.bebc25.fixgo.AI.entity.Chat;
import net.likelion.bebc25.fixgo.AI.service.ChatService;
import net.likelion.bebc25.fixgo.AI.service.OpenAIService;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class OpenAIController {
    private final OpenAIService openAIService;
    private final ChatService chatService;

    @ResponseBody
    @PostMapping("/chat")
    public String chat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OpenAIDto.TestRequest request
    ) {
        return openAIService.generate(userDetails, request.text());
    }

    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OpenAIDto.TestRequest request
    ) {
        return openAIService.generateStream(userDetails, request.text());
    }

    public List<Chat> getChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String userId
    ) {
        return chatService.readAllChats(userDetails, userId);
    }
}
