package com.familiemunshi.common.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("!prod")
public class InMemoryTokenRevocationService implements TokenRevocationService {

    private final Map<String, Long> store = new ConcurrentHashMap<>();

    @Override
    public void revoke(String token, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        store.put(keyFor(token), System.currentTimeMillis() + ttlMillis);
    }

    @Override
    public boolean isRevoked(String token) {
        String key = keyFor(token);
        Long expiry = store.get(key);
        if (expiry == null) {
            return false;
        }
        if (expiry < System.currentTimeMillis()) {
            store.remove(key);
            return false;
        }
        return true;
    }
}
