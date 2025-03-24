package com.example.hireviewserver.domain.interview.job;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public Mono<Long> findIdByName(String name) {
        return jobRepository.findJobByName(name)
                .map(Job::getId);
    }

    public Mono<List<JobResponseDTO>> getJobsByCategoryId(Long categoryId) {
        return jobRepository.findAllByCategoryId(categoryId)
                .map(job -> new JobResponseDTO(job.getId(), job.getName()))
                .collectList();
    }
}
