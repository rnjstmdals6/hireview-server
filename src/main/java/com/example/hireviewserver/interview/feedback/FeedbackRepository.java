package com.example.hireviewserver.interview.feedback;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, Long> {
    @Query("SELECT * FROM feedbacks WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Feedback> findAllByUserId(Long userId, int limit, int offset);

    @Query("SELECT * FROM feedbacks WHERE user_id = :userId")
    Flux<Feedback> findAllByUserId(Long userId);
}
