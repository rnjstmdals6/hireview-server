package com.example.hireviewserver.interview.question;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("question")
@Getter @NoArgsConstructor
public class Question {
    @Id
    private Long id;
    private String job;
    private String question;
    private String answer;
    private Integer priority;
    private Integer difficulty;

    public Question(String job, String question, String answer, Integer priority, Integer difficulty) {
        this.job = job;
        this.question = question;
        this.answer = answer;
        this.priority = priority;
        this.difficulty = difficulty;
    }
}