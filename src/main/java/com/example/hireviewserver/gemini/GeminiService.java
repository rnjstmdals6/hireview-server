package com.example.hireviewserver.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.generativelanguage.googleapis.com").build();
    }

    public Flux<String> generateContentStream(GeminiRequestDTO request) {
        return this.webClient.post()
                .uri(apiUrl + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToFlux(String.class).timeout(Duration.ofSeconds(10))
                .doOnError(error -> {
                    // 에러 발생 시 로그 출력
                    System.err.println("Error occurred during stream: " + error.getMessage());
                })
                .doOnCancel(() -> {
                    // 스트림이 취소되었을 때 로그 출력
                    System.out.println("Stream was canceled");
                });

    }
}
