package com.aura.assistant.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * [JwtTokenProvider]
 * 역할: JWT 토큰의 생성, 복호화, 유효성 검증을 담당합니다.
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long tokenValidityInMilliseconds;

    // 생성자를 통해 application.yml의 값을 주입받습니다.
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        // 초 단위를 밀리초(ms) 단위로 변환 (3600 -> 3,600,000)
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    /**
     * [createToken]
     * 사용자 이메일과 권한을 받아 JWT 토큰을 생성합니다.
     * 이제 1시간(3600초) 후에 만료됩니다.
     */
    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * [getExpirationTime]
     * 추가: 외부(Controller)에서 토큰의 만료 시각을 알 수 있도록 계산해주는 로직
     */
    public long getExpirationTime() {
        return System.currentTimeMillis() + tokenValidityInMilliseconds;
    }

    /**
     * [getEmail]
     * 토큰에서 사용자 이메일을 추출합니다.
     */
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * [validateToken]
     * 토큰이 변조되지 않았는지, 만료되지 않았는지 확인합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}