package com.example.hireviewserver.interview.feedback;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FeedbackSaveEvent extends ApplicationEvent {
    private final String feedback;
    private final String answer;
    private final Double score;
    private final Long questionId;
    private final String email;  // 로그인된 유저의 이메일 추가

    public FeedbackSaveEvent(Object source, String feedback, Double score, Long questionId, String email, String answer) {
        super(source);
        this.feedback = feedback;
        this.score = score;
        this.questionId = questionId;
        this.email = email;
        this.answer = answer;
    }
}
