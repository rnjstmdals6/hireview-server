package com.example.hireviewserver.question;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface QuestionRepository extends ReactiveCrudRepository<Question, Long> {

    @Query("SELECT * FROM question WHERE job = :job ORDER BY RANDOM() LIMIT 2")
    Flux<Question> findRandomQuestionsByJob(String job);
}
