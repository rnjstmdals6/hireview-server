package com.example.hireviewserver.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String streamApiUrl;
    @Value("${gemini.api.origin-url}")
    private String apiUrl;

    private final WebClient webClient;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.generativelanguage.googleapis.com").build();
    }

    public Flux<String> generateContentStream(GeminiRequestDTO request) {
        return this.webClient.post()
                .uri(streamApiUrl + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToFlux(String.class).timeout(Duration.ofSeconds(10))
                .doOnError(error -> {
                    System.err.println("Error occurred during stream: " + error.getMessage());
                })
                .doOnCancel(() -> {
                    System.out.println("Stream was canceled");
                });

    }

    public Mono<String> generateStructuredResponse(GeminiStructuredRequestDTO request) {
        String fullUrl = apiUrl + apiKey;

        return webClient.post()
                .uri(fullUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(errorBody ->
                                        System.err.println("Gemini API Error Response: " + errorBody)
                                )
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Gemini API Error: " + errorBody)))
                )
                .bodyToMono(String.class)
                .doOnNext(responseBody -> {
                    System.out.println("Gemini API Success Response: " + responseBody); // 정상 응답 로깅
                })
                .doOnError(e -> {
                    System.err.println("Error calling Gemini: " + e.getMessage()); // 예외 로깅
                });
    }

}
