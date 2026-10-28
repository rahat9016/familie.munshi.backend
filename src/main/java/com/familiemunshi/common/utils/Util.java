package com.familiemunshi.common.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Locale;

public class Util {
    private Util() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static String generateSalt() {
        return BCrypt.gensalt(12);
    }

    public static String hashPassword(String password, String salt) {
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(
            String rawPassword,
            String storedHash
    ) {
        return BCrypt.checkpw(rawPassword, storedHash);
    }

    public static String toSearchPattern(String search) {

        if (search == null || search.isBlank()) {
            return "%%";
        }

        String normalized = search.trim()
                .toLowerCase(Locale.ROOT);

        String escaped = normalized
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + escaped + "%";
    }
}
