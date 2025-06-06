package com.coffeeplz.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7일

    /**
     * Access Token 생성
     */
    public String generateAccessToken(String email) {
        return generateToken(new HashMap<>(), email, accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String email) {
        return generateToken(new HashMap<>(), email, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * JWT 토큰 생성
     */
    public String generateToken(Map<String, Object> extraClaims, String email, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자명(이메일) 추출
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 토큰에서 만료일 추출
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 토큰에서 특정 클레임 추출
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰에서 모든 클레임 추출
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            throw new IllegalArgumentException("토큰이 만료되었습니다");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new IllegalArgumentException("지원되지 않는 토큰입니다");
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
            throw new IllegalArgumentException("잘못된 형식의 토큰입니다");
        } catch (SignatureException e) {
            log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰 서명입니다");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 null이거나 빈 문자열입니다: {}", e.getMessage());
            throw new IllegalArgumentException("토큰이 null이거나 비어있습니다");
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean isTokenValid(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email)) && !isTokenExpired(token);
    }

    /**
     * 토큰 유효성 검증 (이메일 체크 없이)
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 서명 키 생성
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 