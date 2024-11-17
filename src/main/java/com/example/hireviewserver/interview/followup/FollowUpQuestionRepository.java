package com.example.hireviewserver.interview.followup;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface FollowUpQuestionRepository extends ReactiveCrudRepository<FollowUpQuestion, Long> {
    // 특정 질문 ID에 연결된 꼬리질문 조회
    Flux<FollowUpQuestion> findAllByQuestionId(Long questionId);
}