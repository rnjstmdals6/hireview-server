package com.example.hireviewserver.interview.question;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuestionResponseDTO {
    private Long id;
    private String job;
    private String question;
    private Integer priority;
    private Integer difficulty;
    private String tags;

    public QuestionResponseDTO(Question question, String job) {
        this.job = job;
        this.question = question.getContent();
        this.id = question.getId();
        this.priority = question.getPriority();
        this.difficulty = question.getDifficulty();
        this.tags = question.getTags();
    }
}
