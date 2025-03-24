package com.example.hireviewserver.domain.interview.feedback;

import com.example.hireviewserver.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    @PostMapping(value = "/api/v1/feedback/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getFeedbackByQuestion(@RequestBody FeedbackRequestDTO request, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMapMany(email -> feedbackService.getFeedbackByQuestion(request, email));
    }

    @GetMapping("/api/v1/all-feedback")
    public Mono<PageResponse<FeedbackResponseDTO>> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
             Mono<Principal> principal
    ) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> feedbackService.getAllFeedback(page, size, email));
    }

    @GetMapping("/api/v1/feedback/stat")
    public Mono<FeedbackStatResponseDTO> getFeedbackStat(
            Mono<Principal> principal
    ) {
        return principal
                .map(Principal::getName)
                .flatMap(feedbackService::getFeedbackStat);
    }

    @GetMapping("/api/v2/feedback/stat")
    public Mono<FeedbackAbilityDTO> getUserAverageStats(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(feedbackService::getUserAverageStats);
    }
}
