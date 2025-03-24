package store.hireview.config;

import store.hireview.auth.oauth2.GoogleOAuth2Properties;
import store.hireview.auth.oauth2.KakaoOAuth2Properties;
import store.hireview.auth.oauth2.OAuth2ClientRegistrationFactory;
import store.hireview.auth.oauth2.OAuth2Provider;
import store.hireview.auth.jwt.JwtAuthenticationFilter;
import store.hireview.auth.jwt.JwtAuthenticationManager;
import store.hireview.auth.jwt.JwtTokenProvider;
import store.hireview.auth.oauth2.ReactiveOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties({GoogleOAuth2Properties.class, KakaoOAuth2Properties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveOAuth2SuccessHandler successHandler;
    private final GoogleOAuth2Properties googleOAuth2Properties;
    private final KakaoOAuth2Properties kakaoOAuth2Properties;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        JwtAuthenticationManager authenticationManager = new JwtAuthenticationManager(jwtTokenProvider);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(successHandler))
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository(OAuth2ClientRegistrationFactory factory) {
        ClientRegistration google = factory.create(OAuth2Provider.GOOGLE.getRegistrationId(), googleOAuth2Properties);
        ClientRegistration kakao = factory.create(OAuth2Provider.KAKAO.getRegistrationId(), kakaoOAuth2Properties);
        return new InMemoryReactiveClientRegistrationRepository(google, kakao);
    }
}
