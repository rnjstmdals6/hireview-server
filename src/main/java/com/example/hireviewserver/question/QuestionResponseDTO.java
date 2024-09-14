package com.example.hireviewserver.question;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuestionResponseDTO {
    private String job;
    private String question;
    private Long questionId;

    public QuestionResponseDTO(Question question) {
        this.job = question.getJob();
        this.question = question.getQuestion();
        this.questionId = question.getId();
    }
}
