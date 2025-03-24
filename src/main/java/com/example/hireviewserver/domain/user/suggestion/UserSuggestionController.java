package com.example.hireviewserver.domain.user.suggestion;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class UserSuggestionController {
    private final UserSuggestionService userSuggestionService;

    @PostMapping("/api/v1/user/suggestion")
    public Mono<Void> postUserSuggestion(@RequestBody UserSuggestionRequestDTO userSuggestionRequestDTO, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> userSuggestionService.postUserSuggestion(email, userSuggestionRequestDTO));
    }
}
