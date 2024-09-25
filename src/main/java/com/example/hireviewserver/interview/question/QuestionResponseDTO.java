package com.example.hireviewserver.interview.question;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuestionResponseDTO {
    private String job;
    private String question;
    private Integer priority;
    private Integer difficulty;
    private String answer;
    private Long questionId;

    public QuestionResponseDTO(Question question) {
        this.job = question.getJob();
        this.question = question.getQuestion();
        this.questionId = question.getId();
        this.answer = question.getAnswer();
        this.priority = question.getPriority();
        this.difficulty = question.getDifficulty();
    }
}
