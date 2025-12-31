package com.aura.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [역할] 애플리케이션 보안 설정
 * 모든 API와 정적 리소스에 대한 접근을 허용하여 리액트 화면이 정상적으로 로드되게 합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API 호출을 위해 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        // 리액트 빌드 파일, 3D 모델, CSS/JS 경로를 모두 허용
                        .requestMatchers("/", "/api/**", "/dist/**", "/models/**", "/css/**", "/js/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}