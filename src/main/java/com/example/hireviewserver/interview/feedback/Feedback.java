package com.example.hireviewserver.interview.feedback;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("feedbacks")
@Getter
public class Feedback {
    @Id
    private Long id;

    private final Double score;
    private final String content;
    private final String answer;
    private final Long questionId;
    private final Long userId;
    // R2DBC LocalDateTime > final (x)
    private LocalDateTime createdAt;
    private Double accuracy;
    private Double completeness;
    private Double logicality;

    public Feedback(Double score, String content, String answer, Long questionId, Long userId, Double accuracy, Double completeness, Double logicality) {
        this.score = score;
        this.content = content;
        this.answer = answer;
        this.questionId = questionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.accuracy = accuracy;
        this.completeness = completeness;
        this.logicality = logicality;
    }
}
