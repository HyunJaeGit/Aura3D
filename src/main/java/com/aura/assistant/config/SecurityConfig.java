package com.aura.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig 클래스
 * 애플리케이션의 보안 설정을 담당합니다.
 * 현재는 개발 편의를 위해 CSRF 보안 및 인증을 비활성화합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보안을 끕니다 (버튼 클릭 요청 허용을 위해)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청을 로그인 없이 허용합니다.
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // H2 콘솔 등을 쓸 때 필요

        return http.build();
    }
}