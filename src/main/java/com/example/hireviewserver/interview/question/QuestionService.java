package com.example.hireviewserver.interview.question;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.interview.job.JobService;
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
                                .map(question -> new QuestionResponseDTO(question, jobName))
                );
    }

    public Mono<PageResponseDTO<QuestionResponseDTO>> getAllQuestionsByJob(String job, int page, int size) {
        Mono<Long> total = questionRepository.countByJob(job);
        Flux<QuestionResponseDTO> questions = questionRepository.findAllByJobWithPagination(job, size, page * size)
                .map(question -> new QuestionResponseDTO(question, job));

        return total.zipWith(questions.collectList(), (totalElements, questionList) ->
                new PageResponseDTO<>(questionList, totalElements, page)
        );
    }

    public Mono<Question> findQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    public Mono<List<String>> getUniqueTagsByJob(String job) {
        return jobService.findIdByName(job)
                .flatMapMany(questionRepository::findDistinctTagsByJobId)
                .flatMap(tags -> Flux.fromArray(tags.split(",")))
                .map(String::trim)
                .distinct()
                .collectList();
    }
}
