package com.example.hireviewserver.config;

import com.example.hireviewserver.user.JwtAuthenticationFilter;
import com.example.hireviewserver.user.JwtAuthenticationManager;
import com.example.hireviewserver.user.JwtTokenProvider;
import com.example.hireviewserver.user.ReactiveOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveOAuth2SuccessHandler successHandler;

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
//                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(successHandler)
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}