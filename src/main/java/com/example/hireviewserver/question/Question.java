package com.example.hireviewserver.question;

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

    public Question(String job, String question) {
        this.job = job;
        this.question = question;
    }
}