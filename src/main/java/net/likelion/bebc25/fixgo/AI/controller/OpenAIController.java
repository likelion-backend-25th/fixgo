package net.likelion.bebc25.fixgo.AI.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.AI.dto.OpenAIDto;
import net.likelion.bebc25.fixgo.AI.entity.Chat;
import net.likelion.bebc25.fixgo.AI.service.ChatService;
import net.likelion.bebc25.fixgo.AI.service.OpenAIService;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI API", description = "관리자용 AI 챗봇 API")
@RestController
@RequestMapping("/api/v1/ai")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class OpenAIController {
    private final OpenAIService openAIService;
    private final ChatService chatService;

    @ResponseBody
    @PostMapping("/chat/test")
    @Operation(summary = "BrunoTest용", description = "사용자의 채팅을 입력받아 그에 맞는 답을 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI 응답 성공"),
    })
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

    @ResponseBody
    @GetMapping
    public List<Chat> getChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return chatService.readAllChats(userDetails);
    }
}
