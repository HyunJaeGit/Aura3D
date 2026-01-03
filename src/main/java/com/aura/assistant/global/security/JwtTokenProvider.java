package com.aura.assistant.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * [JwtTokenProvider]
 * 역할: JWT 토큰의 생성, 복호화, 유효성 검증을 담당합니다.
 */
@Component
public class JwtTokenProvider {

    // 포트폴리오용 비밀키 (실무에서는 환경변수나 설정파일로 관리해야 합니다)
    private final String secretKey = "Aura3DSystemMonitoringSecretKeyForJwtTokenGeneration";
    private final Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

    // 토큰 유효시간: 24시간
    private final long tokenValidityInMilliseconds = 1000L * 60 * 60 * 24;

    /**
     * [createToken]
     * 사용자 이메일과 권한을 받아 JWT 토큰을 생성합니다.
     */
    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role); // 토큰에 사용자 권한 정보 포함

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