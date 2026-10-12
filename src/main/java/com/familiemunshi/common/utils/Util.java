package com.familiemunshi.common.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class Util {
    private Util() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static String generateSalt() {
        return BCrypt.gensalt();
    }

    public static String hashPassword(String password, String salt) {
        return BCrypt.hashpw(password, salt);
    }
}
