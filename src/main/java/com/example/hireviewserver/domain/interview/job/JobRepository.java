package com.example.hireviewserver.domain.interview.job;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface JobRepository extends ReactiveCrudRepository<Job, Long> {
    Mono<Job> findJobByName(String name);

    Flux<Job> findAllByCategoryId(Long categoryId);
}
