package com.example.hireviewserver.user;

import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/api/v1/user")
    public Mono<UserInfoResponseDTO> getUserInfo(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserByEmail)
                .map(UserInfoResponseDTO::new);
    }

    @PutMapping("/api/v1/user/name")
    public Mono<Void> setUsername(@RequestBody SetUserNameRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> userService.setUserName(email, dto));
    }

    @PutMapping("/api/v1/user/job")
    public Mono<Void> setUserJob(@RequestBody SetUserJobRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> userService.setUserJob(email, dto));
    }

    @PutMapping("/api/v1/user/token")
    public Mono<Void> consumeToken(@RequestBody SetUserJobRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::consumeToken)
                .then();
    }

    @DeleteMapping("/api/v1/user")
    public Mono<Void> setUserJob(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::deleteUser);
    }

    @PostMapping("/api/token/v1/refresh")
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