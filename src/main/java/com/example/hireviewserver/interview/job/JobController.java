package com.example.hireviewserver.interview.job;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping("/api/v1/job/all")
    @Operation(summary = "카테고리 아이디로 모든 직무 반환합니다.")
    public Mono<List<JobResponseDTO>> getJobsByCategoryId(@RequestParam("categoryId") Long categoryId) {
        return jobService.getJobsByCategoryId(categoryId);
    }
}