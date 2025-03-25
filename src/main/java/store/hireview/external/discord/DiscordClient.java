package store.hireview.external.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<Void> post(String webhookUrl, DiscordRequestDTO request) {
        return webClientBuilder
                .build()
                .post()
                .uri(webhookUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of("content", request.toDiscordFormat()))
                .retrieve()
                .bodyToMono(Void.class);
    }
}