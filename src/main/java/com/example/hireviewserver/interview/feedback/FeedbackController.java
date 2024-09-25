package com.example.hireviewserver.interview.feedback;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    @PostMapping(value = "/api/v1/feedback/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getFeedbackByQuestion(@RequestBody FeedbackRequestDTO request) {
        return feedbackService.getFeedbackByQuestion(request);
    }
}
