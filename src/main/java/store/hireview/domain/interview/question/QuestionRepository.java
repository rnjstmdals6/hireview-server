package store.hireview.domain.interview.question;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface QuestionRepository extends ReactiveCrudRepository<Question, Long> {

    @Query("SELECT * FROM questions WHERE job_id = :jobId ORDER BY RAND() LIMIT 5")
    Flux<Question> findRandomQuestionsByJobId(Long jobId);

    @Query("SELECT * FROM questions WHERE job_id = :jobId AND (IFNULL(:tag, '') = '' OR tags LIKE CONCAT('%', :tag, '%')) ORDER BY RAND()")
    Flux<Question> findByJobIdAndTag(Long jobId, String tag);

    @Query("SELECT COUNT(*) FROM questions WHERE job_id = :jobId")
    Mono<Long> countByJobId(Long jobId);

    @Query("SELECT DISTINCT tags FROM questions WHERE job_id = :jobId")
    Flux<String> findDistinctTagsByJobId(Long jobId);
}
