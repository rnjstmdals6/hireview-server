package store.hireview.external.gemini;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    public Flux<String> postStream(String path, GeminiRequestDTO request) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri(path)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToFlux(String.class);
    }

    public Mono<String> post(String path, GeminiStructuredRequestDTO request) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri(path)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("🔥 Gemini Error Response Body:");
                                    System.err.println(errorBody);
                                    return Mono.error(new RuntimeException("Gemini API Error: " + errorBody));
                                })
                )
                .bodyToMono(String.class)
                .doOnNext(responseBody -> {
                    System.out.println("✅ Gemini Success Response Body:");
                    System.out.println(responseBody);
                });
    }
}
