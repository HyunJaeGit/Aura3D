package com.aura.assistant.domain.user;

import com.aura.assistant.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * [UserController]
 * 역할: 회원가입 및 JWT 기반 로그인 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성기 주입

    /**
     * [회원가입]
     */
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody Map<String, String> request) {
        userService.join(
                request.get("email"),
                request.get("password"),
                request.get("name")
        );
        return ResponseEntity.ok("회원가입 성공!");
    }

    /**
     * [로그인]
     * 수정 사항: 인증 성공 시 JWT 토큰과 사용자 정보를 JSON으로 반환합니다.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        // 1. UserService를 통해 사용자 인증 및 객체 가져오기
        // (기존 boolean login 대신 User 객체를 반환하는 authenticate 메서드 사용 권장)
        User user = userService.authenticate(email, password);

        // 2. JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());

        // 3. 프론트엔드에 전달할 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("name", user.getName());
        responseData.put("role", user.getRole());

        // 리액트 헤더의 카운트다운을 위해 현재 시각 + 1시간 값을 전달
        responseData.put("expiresAt", jwtTokenProvider.getExpirationTime());
        responseData.put("message", "로그인에 성공하였습니다.");

        return ResponseEntity.ok(responseData);
    }
}