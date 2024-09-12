package com.example.hireviewserver.feedback;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, Long> {
}
