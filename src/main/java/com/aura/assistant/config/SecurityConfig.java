package com.aura.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [SecurityConfig]
 * 역할: 애플리케이션 보안 설정 및 비밀번호 암호화 도구 등록
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호를 안전하게 암호화해주는 도구(Bean)입니다.
     * 회원가입 시 비밀번호를 DB에 그냥 저장하지 않고 이 도구로 암호화해서 저장합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 실질적인 보안 규칙을 정의하는 곳입니다.
     * 메서드가 중복되지 않도록 하나로 합쳤습니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API 통신 시 간섭을 막기 위해 CSRF 보안을 잠시 꺼둡니다.
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // 홈페이지, API, 리액트 정적 파일들은 로그인 없이도 볼 수 있게 허용합니다.
                        .requestMatchers("/", "/api/**", "/dist/**", "/models/**", "/css/**", "/js/**").permitAll()
                        // 그 외 모든 요청도 일단은 허용합니다. (개발 단계 편의상)
                        .anyRequest().permitAll()
                )

                // H2 콘솔이나 특정 프레임 구조를 사용할 수 있게 해주는 설정입니다.
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }
}