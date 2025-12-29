package com.aura.assistant.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig 클래스
 * 스프링 시큐리티 설정을 담당합니다.
 * 현재는 개발 편의를 위해 로그인 창을 비활성화하고 모든 접근을 허용합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 기능을 잠시 끕니다 (이게 켜져 있으면 외부에서 POST 명령을 내릴 수 없습니다).
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 모든 페이지와 API에 대해 로그인 없이 접근할 수 있도록 허용합니다.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 3. 기본으로 제공되는 로그인 폼 화면을 사용하지 않습니다.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}