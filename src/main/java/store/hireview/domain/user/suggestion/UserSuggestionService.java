package store.hireview.domain.user.suggestion;

import store.hireview.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import store.hireview.external.discord.DiscordGateway;
import store.hireview.external.discord.DiscordRequestDTO;

@Service
@RequiredArgsConstructor
public class UserSuggestionService {
    private final UserService userService;
    private final UserSuggestionRepository userSuggestionRepository;
    private final DiscordGateway discordGateway;
    public Mono<Void> postUserSuggestion(String email, UserSuggestionRequestDTO dto) {
        return userService.findUserIdByEmail(email)
                .flatMap(userId ->
                        userSuggestionRepository.save(new UserSuggestion(userId, dto))
                                .then(discordGateway.sendFeedback("New User Suggestion", email, dto.getContent(), dto.getEvaluation()))
                );
    }
}
