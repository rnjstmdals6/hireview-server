package com.example.hireviewserver.feedback;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedbackSaveEvent extends ApplicationEvent {
    private final String feedback;
    private final int score;
    private final Long questionId;

    public FeedbackSaveEvent(Object source, String feedback, int score, Long questionId) {
        super(source);
        this.feedback = feedback;
        this.score = score;
        this.questionId = questionId;
    }
}
