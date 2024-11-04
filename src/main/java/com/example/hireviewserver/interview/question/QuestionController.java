package com.example.hireviewserver.interview.question;

import com.example.hireviewserver.common.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/api/v1/questions")
    @Operation(summary = "직무에 관련된 무작위 5개의 질문 반환한다.", description = "Fetch 5 random questions for a specific job")
    public Flux<QuestionResponseDTO> getRandomQuestions(@RequestParam String job) {
        return questionService.getRandomQuestionsByJob(job);
    }

    @GetMapping("/api/v1/questions/all")
    @Operation(summary = "직무에 관련된 모든 질문을 반환한다.")
    public Mono<PageResponseDTO<QuestionResponseDTO>> getAllQuestionsByJob(
            @RequestParam Long jobId,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return questionService.findAllByJobAndTagWithPagination(jobId, tag, page, size);
    }

    @GetMapping("/api/v1/questions/specializations")
    @Operation(summary = "특정 직무에 관련된 고유한 특화 분야(태그) 목록을 반환합니다.",
            description = "지정된 직무에 해당하는 모든 질문을 조회하여 중복되지 않는 고유한 특화 분야(태그) 목록을 반환합니다.")
    public Mono<List<String>> getSpecializations(@RequestParam String job) {
        return questionService.getUniqueTagsByJob(job);
    }
}
