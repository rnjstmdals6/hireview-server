package com.example.hireviewserver.common;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @GetMapping("/common/jobs")
    public Mono<JobResponseDTO> getJobList() {
        return commonService.getJobs();
    }
}
