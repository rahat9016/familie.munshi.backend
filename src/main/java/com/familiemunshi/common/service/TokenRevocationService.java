package com.familiemunshi.common.service;

import com.familiemunshi.common.exceptions.TokenSecurityException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public interface TokenRevocationService {

    void revoke(String token, long ttlMillis);

    boolean isRevoked(String token);

    default String keyFor(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenSecurityException("SHA-256 algorithm not available", e);
        }
    }
}
