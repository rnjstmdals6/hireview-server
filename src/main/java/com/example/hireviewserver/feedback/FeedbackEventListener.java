package com.example.hireviewserver.feedback;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackEventListener {
    private final FeedbackRepository feedbackRepository;

    @Async
    @EventListener
    public void handleFeedbackSaveEvent(FeedbackSaveEvent event) {
        Feedback feedback = new Feedback(event.getScore(), event.getFeedback(), event.getQuestionId());
        feedbackRepository.save(feedback).subscribe();
    }
}