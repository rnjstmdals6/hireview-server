package store.hireview.external.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import store.hireview.common.util.DiscordMessageUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordGateway {

    private final DiscordClient discordClient;

    @Value("${external.discord.feedback-webhook-url}")
    private String feedbackWebhookUrl;
    @Value("${external.discord.new-user-webhook-url}")
    private String newUserWebhookUrl;

    public Mono<Void> sendNewUserNotification(String username, String email, long totalUsers) {
        String message = DiscordMessageUtil.newUserMessage(username, email, totalUsers);
        return discordClient.post(newUserWebhookUrl, new DiscordRequestDTO(message));
    }

    public Mono<Void> sendFeedback(String title, String sender, String content, String evaluation) {
        String message = DiscordMessageUtil.feedbackMessage(title, sender, content, evaluation);
        return discordClient.post(feedbackWebhookUrl, new DiscordRequestDTO(message));
    }
}