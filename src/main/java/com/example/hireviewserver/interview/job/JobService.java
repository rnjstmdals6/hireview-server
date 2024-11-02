package com.example.hireviewserver.interview.job;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public Mono<Long> findIdByName(String name) {
        return jobRepository.findJobByName(name)
                .map(Job::getId);
    }
}
