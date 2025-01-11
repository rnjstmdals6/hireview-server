package com.example.hireviewserver.user.suggestion;

import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserSuggestionService {
    private final UserService userService;

    private final UserSuggestionRepository userSuggestionRepository;
    public Mono<Void> postUserSuggestion(String email, UserSuggestionRequestDTO dto) {
        return userService.findUserIdByEmail(email)
                .flatMap(userId -> userSuggestionRepository.save(new UserSuggestion(userId, dto)))
                .then();
    }
}
