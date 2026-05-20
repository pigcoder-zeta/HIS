package com.smarthealthcare.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(username)
                .claims(Map.of("userId", userId, "role", roleCode))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * 从Token中解析Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }
}
