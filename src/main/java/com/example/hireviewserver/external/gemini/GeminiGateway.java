package com.example.hireviewserver.external.gemini;

import com.example.hireviewserver.common.constant.GeminiLogMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiGateway {

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String streamApiUrl;
    @Value("${gemini.api.origin-url}")
    private String apiUrl;
    @Value("${gemini.timeout-seconds}")
    private int timeoutSeconds;

    private final GeminiClient geminiClient;
    public Flux<String> generateContentStream(GeminiRequestDTO request) {
        return geminiClient.postStream(streamApiUrl + apiKey, request)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnError(error -> log.error(GeminiLogMessages.GEMINI_STREAM_ERROR, error))
                .doOnCancel(() -> log.warn(GeminiLogMessages.GEMINI_STREAM_CANCEL));
    }

    public Mono<String> generateStructuredResponse(GeminiStructuredRequestDTO request) {
        return geminiClient.post(apiUrl + apiKey, request)
                .doOnError(error -> log.error(GeminiLogMessages.GEMINI_CALL_ERROR, error));
    }
}
