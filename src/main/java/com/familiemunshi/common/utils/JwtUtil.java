package com.familiemunshi.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long tokenExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private SecretKey secretKey;


    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateUserToken(UUID userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("jwtToken", "userToken")
                .claim("tokenPurpose", "access")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + tokenExpirationMs
                ))
                .signWith(secretKey)
                .compact();
    }

    public String generateUserRefreshToken(UUID userId, String email) {
        return Jwts.builder().subject(userId.toString()).claim("email", email).claim("jwtToken", "userToken").claim("tokenPurpose", "refresh").id(UUID.randomUUID().toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + refreshExpirationMs)).signWith(secretKey).compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public boolean validateUserToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return "userToken".equals(claims.get("jwtToken", String.class)) && "access".equals(claims.get("tokenPurpose", String.class));

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}