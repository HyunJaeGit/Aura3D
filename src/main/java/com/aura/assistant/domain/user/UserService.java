package com.aura.assistant.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [UserService]
 * 역할: 회원가입 및 JWT 발행을 위한 사용자 인증을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * [회원가입]
     * User 엔티티의 @Builder를 사용하여 객체를 생성합니다.
     */
    @Transactional
    public void join(String email, String password, String name) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    /**
     * [authenticate - 사용자 인증]
     * UserController에서 호출하는 메서드입니다.
     * 인증 성공 시 User 객체를 반환하여 JWT 토큰 생성을 돕습니다.
     */
    public User authenticate(String email, String password) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 검증 (암호화된 비번과 비교)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 인증 성공 시 유저 엔티티 반환
        return user;
    }
}