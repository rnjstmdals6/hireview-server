package store.hireview.domain.interview.question;

import store.hireview.domain.interview.job.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final JobService jobService;
    public Flux<QuestionResponseDTO> getRandomQuestionsByJob(String jobName) {
        return jobService.findIdByName(jobName)
                .flatMapMany(jobId ->
                        questionRepository.findRandomQuestionsByJobId(jobId)
                                .map(QuestionResponseDTO::new)
                );
    }

    public Flux<QuestionResponseDTO> findAllByJobAndTag(Long jobId, String tag) {
        return questionRepository.findByJobIdAndTag(jobId, tag)
                .map(QuestionResponseDTO::new);
    }

    public Mono<Question> findQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    public Mono<List<String>> getUniqueTagsByJob(Long jobId) {
        return questionRepository.findDistinctTagsByJobId(jobId)
                .flatMap(tags -> Flux.fromArray(tags.split(",")))
                .map(String::trim)
                .distinct()
                .collectList();
    }
}
