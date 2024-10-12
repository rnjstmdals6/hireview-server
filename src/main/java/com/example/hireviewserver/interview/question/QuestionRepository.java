package com.example.hireviewserver.interview.question;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

@Repository
public interface QuestionRepository extends ReactiveCrudRepository<Question, Long> {

    @Query("SELECT * FROM questions WHERE job = :job ORDER BY RAND() LIMIT 2")
    Flux<Question> findRandomQuestionsByJob(String job);

    @Query("SELECT * FROM questions WHERE (:job IS NULL OR job = :job) LIMIT :limit OFFSET :offset")
    Flux<Question> findAllByJobWithPagination(@Nullable String job, int limit, int offset);

    @Query("SELECT COUNT(*) FROM questions WHERE job = :job")
    Mono<Long> countByJob(String job);
}
