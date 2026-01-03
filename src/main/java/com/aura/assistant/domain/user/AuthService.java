package com.aura.assistant.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * [AuthService]
 * 역할: 사용자 인증(Login) 로직을 처리합니다.
 * 현업 포인트: 클래스명을 서비스의 목적(Auth)에 맞게 간결화하고 UserDetailsService를 구현합니다.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 스프링 시큐리티가 로그인 시 사용자의 존재 여부를 확인하는 메서드입니다.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. DB에서 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다: " + email));

        // 2. 스프링 시큐리티 전용 User 객체(UserDetails)를 생성하여 반환
        // 이 정보와 사용자가 입력한 패스워드를 시큐리티가 비교하게 됩니다.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // DB에 저장된 암호화된 비밀번호
                .roles(user.getRole().name()) // USER 또는 ADMIN 권한 부여
                .build();
    }
}