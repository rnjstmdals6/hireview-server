package com.example.hireviewserver.interview.question;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

@Repository
public interface QuestionRepository extends ReactiveCrudRepository<Question, Long> {

    @Query("SELECT * FROM questions WHERE job_id = :jobId ORDER BY RAND() LIMIT 2")
    Flux<Question> findRandomQuestionsByJobId(Long jobId);

    @Query("SELECT * FROM questions WHERE job_id = :jobId AND (IFNULL(:tag, '') = '' OR tags LIKE CONCAT('%', :tag, '%')) LIMIT :limit OFFSET :offset")
    Flux<Question> findAllByJobIdAndTagWithPagination(Long jobId, @Nullable String tag, int limit, int offset);

    @Query("SELECT COUNT(*) FROM questions WHERE job_id = :jobId")
    Mono<Long> countByJobId(Long jobId);

    @Query("SELECT DISTINCT tags FROM questions WHERE job_id = :jobId")
    Flux<String> findDistinctTagsByJobId(Long jobId);
}
