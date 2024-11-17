package com.example.hireviewserver.interview.feedback;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("feedbacks")
@Getter
public class Feedback {
    @Id
    private Long id;

    private final Double score;
    private final String feedback;
    private final String answer;
    private final Long questionId;
    private final Long userId;
    private final LocalDateTime createdAt;

    public Feedback(Double score, String feedback, String answer, Long questionId, Long userId) {
        this.score = score;
        this.feedback = feedback;
        this.answer = answer;
        this.questionId = questionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
