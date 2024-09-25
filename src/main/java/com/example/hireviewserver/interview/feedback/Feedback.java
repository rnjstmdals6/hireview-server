package com.example.hireviewserver.interview.feedback;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("feedback")
@Getter
public class Feedback {
    @Id
    private Long id;

    private final Integer score;
    private final String feedback;
    private final Long questionId;

    public Feedback(Integer score, String feedback, Long questionId) {
        this.score = score;
        this.feedback = feedback;
        this.questionId = questionId;
    }
}
