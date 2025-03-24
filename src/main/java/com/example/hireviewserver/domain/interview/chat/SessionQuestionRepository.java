package com.example.hireviewserver.domain.interview.chat;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionQuestionRepository extends ReactiveCrudRepository<SessionQuestion, Long> {

    @Query("SELECT * FROM session_questions WHERE session_id = :sessionId ORDER BY step")
    Flux<SessionQuestion> findBySessionId(String sessionId);

    @Query("SELECT * FROM session_questions WHERE session_id = :sessionId AND step = :step LIMIT 1")
    Mono<SessionQuestion> findBySessionIdAndStep(String sessionId, int step);
}