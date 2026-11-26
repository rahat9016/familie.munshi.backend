package com.familiemunshi.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenRevocationServiceTest {

    private InMemoryTokenRevocationService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryTokenRevocationService();
    }

    @Test
    void isRevoked_tokenNeverRevoked_returnsFalse() {
        assertThat(service.isRevoked("some.jwt.token")).isFalse();
    }

    @Test
    void isRevoked_tokenRevokedWithFutureTtl_returnsTrue() {
        service.revoke("some.jwt.token", 60_000);

        assertThat(service.isRevoked("some.jwt.token")).isTrue();
    }

    @Test
    void revoke_nonPositiveTtl_doesNotStoreToken() {
        service.revoke("expired.token", 0);
        service.revoke("already.expired", -1);

        assertThat(service.isRevoked("expired.token")).isFalse();
        assertThat(service.isRevoked("already.expired")).isFalse();
    }

    @Test
    void isRevoked_ttlAlreadyElapsed_returnsFalse() throws InterruptedException {
        service.revoke("short.lived.token", 1);
        Thread.sleep(5);

        assertThat(service.isRevoked("short.lived.token")).isFalse();
    }

    @Test
    void revoke_differentTokens_isolatedFromEachOther() {
        service.revoke("token.a", 60_000);

        assertThat(service.isRevoked("token.a")).isTrue();
        assertThat(service.isRevoked("token.b")).isFalse();
    }
}
