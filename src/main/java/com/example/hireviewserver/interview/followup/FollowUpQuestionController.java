package com.example.hireviewserver.interview.followup;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/follow-up-questions")
@RequiredArgsConstructor
public class FollowUpQuestionController {
    private final FollowUpQuestionService followUpQuestionService;

    @PostMapping
    public Flux<String> createFollowUpQuestion(@RequestBody FollowUpQuestionRequestDTO request) {
        return followUpQuestionService.generateFollowUpQuestion(request.getQuestionId(), request.getAnswer());
    }

    // 특정 질문 ID로 연결된 모든 꼬리질문 조회
    @GetMapping("/{questionId}")
    public Flux<FollowUpQuestion> getFollowUpQuestions(@PathVariable Long questionId) {
        return followUpQuestionService.getFollowUpQuestionsByQuestionId(questionId);
    }
}