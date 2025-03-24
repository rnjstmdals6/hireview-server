package store.hireview.domain.interview.feedback;

import store.hireview.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackEventListener {
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    @Async
    @EventListener
    public void handleFeedbackSaveEvent(FeedbackSaveEvent event) {
        userService.findUserIdByEmail(event.getEmail())
                .flatMap(userId -> {
                    Feedback feedback = new Feedback(event.getScore(), event.getFeedback(), event.getAnswer(),event.getQuestionId(), userId, event.getAccuracy(), event.getCompleteness(), event.getLogicality());
                    return feedbackRepository.save(feedback);
                })
                .subscribe();
    }
}