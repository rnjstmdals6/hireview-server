package store.hireview.external.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordGateway {

    private final DiscordClient discordClient;

    @Value("${external.discord.webhook-url}")
    private String webhookUrl;

    public Mono<Void> sendMessage(DiscordRequestDTO dto) {
        return discordClient.post(webhookUrl, dto);
    }
}