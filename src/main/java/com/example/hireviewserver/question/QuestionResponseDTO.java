package com.example.hireviewserver.question;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuestionResponseDTO {
    private String job;
    private String question;

    public QuestionResponseDTO(Question question) {
        this.job = question.getJob();
        this.question = question.getQuestion();
    }
}
