package com.example.hireviewserver.domain.interview.followup;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FollowUpQuestionController {
    private final FollowUpQuestionService followUpQuestionService;

    @PostMapping(value ="/api/v1/follow-up/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> createFollowUpQuestion(@RequestBody FollowUpQuestionRequestDTO request, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMapMany(email -> followUpQuestionService.generateFollowUpQuestion(request, email));
    }

    // 특정 질문 ID로 연결된 모든 꼬리질문 조회
    @GetMapping("/api/v1/follow-up/{questionId}")
    public Flux<FollowUpQuestion> getFollowUpQuestions(@PathVariable Long questionId) {
        return followUpQuestionService.getFollowUpQuestionsByQuestionId(questionId);
    }
}