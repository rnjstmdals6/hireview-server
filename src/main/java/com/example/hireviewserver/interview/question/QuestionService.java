package com.example.hireviewserver.interview.question;

import com.example.hireviewserver.common.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Flux<QuestionResponseDTO> getRandomQuestionsByJob(String job) {
        return questionRepository.findRandomQuestionsByJob(job)
                .map(question -> new QuestionResponseDTO(question, job));
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
}
