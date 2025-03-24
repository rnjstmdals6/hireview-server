package com.example.hireviewserver.config;

import com.example.hireviewserver.domain.user.JwtAuthenticationFilter;
import com.example.hireviewserver.domain.user.JwtAuthenticationManager;
import com.example.hireviewserver.domain.user.JwtTokenProvider;
import com.example.hireviewserver.domain.user.ReactiveOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveOAuth2SuccessHandler successHandler;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String kakaoAuthorizationUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-name-attribute}")
    private String kakaoUserNameAttribute;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, ReactiveOAuth2SuccessHandler successHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        JwtAuthenticationManager authenticationManager = new JwtAuthenticationManager(jwtTokenProvider);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(successHandler)
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration googleRegistration = googleClientRegistration();
        ClientRegistration kakaoRegistration = kakaoClientRegistration();

        return new InMemoryReactiveClientRegistrationRepository(googleRegistration, kakaoRegistration);
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .redirectUri(googleRedirectUri)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
    }

    private ClientRegistration kakaoClientRegistration() {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId(kakaoClientId)
                .clientSecret(kakaoClientSecret)
                .redirectUri(kakaoRedirectUri)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("profile_nickname", "account_email")
                .authorizationUri(kakaoAuthorizationUri)
                .tokenUri(kakaoTokenUri)
                .userInfoUri(kakaoUserInfoUri)
                .userNameAttributeName(kakaoUserNameAttribute)
                .clientName("Kakao")
                .build();
    }
}
