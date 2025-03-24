package store.hireview.domain.interview.chat;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface InterviewSessionRepository extends ReactiveCrudRepository<InterviewSession, Long> {
    Mono<InterviewSession> findBySessionId(String sessionId);
    Mono<InterviewSession> findByUserId(Long userId);
}