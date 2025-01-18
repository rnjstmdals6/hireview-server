package com.example.hireviewserver.interview.feedback;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedbackSaveEvent extends ApplicationEvent {
    private final String feedback;
    private final String answer;
    private final Double score;
    private final Long questionId;
    private final String email;
    private final Double accuracy;
    private final Double completeness;
    private final Double logicality;

    public FeedbackSaveEvent(Object source, String feedback, Double score, Long questionId, String email, String answer, Double accuracy, Double completeness, Double logicality) {
        super(source);
        this.feedback = feedback;
        this.score = score;
        this.questionId = questionId;
        this.email = email;
        this.answer = answer;
        this.accuracy = accuracy;
        this.completeness = completeness;
        this.logicality = logicality;
    }
}
