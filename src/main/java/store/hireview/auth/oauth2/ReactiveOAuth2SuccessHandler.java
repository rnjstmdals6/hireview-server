package store.hireview.auth.oauth2;

import store.hireview.auth.jwt.JwtTokenProvider;
import store.hireview.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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

    @Value("${app.redirect-url}")
    private String redirectUrl;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String email;
        String name;
        String picture;

        if (oauthToken.getAuthorizedClientRegistrationId().equals("google")) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
        } else if (oauthToken.getAuthorizedClientRegistrationId().equals("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

            email = (String) kakaoAccount.get("email");
            name = (String) properties.get("nickname");

            picture = (String) properties.getOrDefault("profile_image", "");
            if (picture == null || picture.isEmpty()) {
                picture = "";
            }
        } else {
            return Mono.error(new IllegalStateException("지원하지 않는 로그인 타입입니다."));
        }

        // 사용자 정보 처리 및 JWT 생성
        return userService.findOrCreateUser(email, name, picture)
                .flatMap(user -> {
                    String token = jwtTokenProvider.generateToken(user.getEmail());
                    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

                    exchange.getResponse().addCookie(ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .path("/")
                            .build());

                    String finalRedirectUrl = redirectUrl + "?token=" + token;
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create(finalRedirectUrl));

                    return exchange.getResponse().setComplete();
                })
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authentication))));
    }
}