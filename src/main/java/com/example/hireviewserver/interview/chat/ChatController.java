package com.example.hireviewserver.interview.chat;

import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final InterviewSessionService sessionService;
    private final UserService userService;

    @PostMapping("/session/{jobId}")
    public Mono<InterviewSession> createSession(Principal principal, @PathVariable Long jobId) {
        return sessionService.createSession(principal.getName(), jobId);
    }

    @GetMapping(value = "/interview/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessageResponseDTO> interview(@PathVariable String sessionId) {
        return chatService.streamLiveMessages(sessionId)
                .concatMap(msg -> {
                    boolean isAI = "AI".equals(msg.getSender());
                    return Mono.just(ChatMessageResponseDTO.from(msg, isAI))
                            .delayElement(isAI ? Duration.ofSeconds(1) : Duration.ZERO);
                });
    }

    @PostMapping("/message/{sessionId}")
    public Mono<Void> processUserMessage(
            @PathVariable String sessionId,
            @RequestBody ChatMessageRequestDTO request
    ) {
        return sessionService.getCurrentStep(sessionId)
                .flatMap(step ->
                        chatService.processUserResponse(sessionId, request.getUserMessage(), step)
                                .then(sessionService.incrementStep(sessionId))
                )
                .then();
    }

    @GetMapping("/history/{sessionId}")
    public Flux<ChatMessage> getChatHistory(Principal principal, @PathVariable String sessionId) {
        return sessionService.getSession(sessionId)
                .flatMapMany(session ->
                        userService.findUserByEmail(principal.getName())
                                .flatMapMany(user -> {
                                    if (user.getId().equals(session.getUserId())) {
                                        return chatService.getMessages(sessionId);
                                    } else {
                                        return Flux.error(new RuntimeException("Unauthorized access."));
                                    }
                                })
                );
    }
}