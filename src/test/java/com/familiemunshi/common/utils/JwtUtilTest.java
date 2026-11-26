package com.familiemunshi.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final long ACCESS_EXPIRATION_MS = 900_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-test-secret-test-secret-test-secret-0123456789");
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMs", ACCESS_EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", REFRESH_EXPIRATION_MS);
        jwtUtil.init();
    }

    @Test
    void getRemainingMillis_freshAccessToken_isCloseToExpiration() {
        String token = jwtUtil.generateUserToken(UUID.randomUUID(), "user@example.com");

        long remaining = jwtUtil.getRemainingMillis(token);

        assertThat(remaining).isPositive().isLessThanOrEqualTo(ACCESS_EXPIRATION_MS);
        assertThat(remaining).isGreaterThan(ACCESS_EXPIRATION_MS - 10_000);
    }

    @Test
    void getRemainingMillis_malformedToken_returnsZero() {
        assertThat(jwtUtil.getRemainingMillis("not-a-jwt")).isZero();
    }
}
