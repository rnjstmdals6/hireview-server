package com.example.hireviewserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해 CORS 설정
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "https://hireview.net")  // 허용할 도메인 설정
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 허용할 HTTP 메서드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true)  // 자격 증명 허용
                .maxAge(3600);  // 요청 캐시 시간 (1시간)
    }
}
