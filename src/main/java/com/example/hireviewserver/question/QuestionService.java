package com.example.hireviewserver.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Flux<QuestionResponseDTO> getRandomQuestionsByJob(String job) {
        return questionRepository.findRandomQuestionsByJob(job)
                .map(QuestionResponseDTO::new);
    }
}
