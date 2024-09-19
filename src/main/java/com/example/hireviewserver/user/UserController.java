package com.example.hireviewserver.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/api/v1/user-info")
    public Mono<UserInfoResponseDTO> getUserInfo(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUser);
    }

    @PostMapping("/api/token/refresh")
    public Mono<TokenResponseDTO> refreshAccessToken(@RequestBody TokenRequestDTO tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();

        return Mono.just(refreshToken)
                .filter(jwtTokenProvider::validateToken) // 토큰 검증
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid refresh token")))
                .map(jwtTokenProvider::getEmailFromToken)
                .map(jwtTokenProvider::generateToken)
                .map(newAccessToken -> new TokenResponseDTO(newAccessToken, refreshToken));
    }
}