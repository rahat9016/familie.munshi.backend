package com.familiemunshi.common.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Profile("prod")
public class RedisTokenRevocationService implements TokenRevocationService {

    private static final String PREFIX = "revoked:token:";

    private final StringRedisTemplate redis;

    public RedisTokenRevocationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void revoke(String token, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        redis.opsForValue().set(PREFIX + keyFor(token), "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isRevoked(String token) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + keyFor(token)));
    }
}
