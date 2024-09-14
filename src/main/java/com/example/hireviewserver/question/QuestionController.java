package com.example.hireviewserver.question;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/api/v1/questions")
    @Operation(summary = "직무에 관련된 무작위 5개의 질문 반환한다.", description = "Fetch 5 random questions for a specific job")
    public Flux<QuestionResponseDTO> getAllQuestions(@RequestParam String job) {
        return questionService.getRandomQuestionsByJob(job).doOnNext(question -> {
            System.out.println(question.getQuestion());  // 질문을 콘솔에 출력
        });
    }
}
