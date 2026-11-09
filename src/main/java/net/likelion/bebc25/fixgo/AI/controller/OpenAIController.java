package net.likelion.bebc25.fixgo.AI.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.AI.dto.OpenAIDto;
import net.likelion.bebc25.fixgo.AI.service.OpenAIService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class OpenAIController {
    private final OpenAIService openAIService;


    @ResponseBody
    @PostMapping("/chat")
    public String chat(@RequestBody OpenAIDto.TestRequest request) {
        return openAIService.generate(request.text());
    }

    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@RequestBody OpenAIDto.TestRequest request) {
        return openAIService.generateStream(request.text());
    }
}
