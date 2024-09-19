package com.example.hireviewserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReactiveOAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        // 인증된 사용자 정보에서 속성 추출
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        // 사용자 정보 생성/조회 후 처리
        return userService.findOrCreateUser(email, name, picture)
                .flatMap(user -> {
                    // JWT 토큰 생성
                    String token = jwtTokenProvider.generateToken(user.getEmail());
                    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

                    // 쿠키에 refreshToken 저장
                    exchange.getResponse().addCookie(ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .path("/")
                            .build());

                    // 리다이렉션 처리
                    String redirectUrl = "http://127.0.0.1:5173?token=" + token;
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));

                    return exchange.getResponse().setComplete();
                })
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authentication))));
    }
}