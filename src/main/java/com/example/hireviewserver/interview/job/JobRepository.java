package com.example.hireviewserver.interview.job;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends ReactiveCrudRepository<Job, Long> {
}
