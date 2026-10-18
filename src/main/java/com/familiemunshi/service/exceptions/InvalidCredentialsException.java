package com.familiemunshi.service.exceptions;
import org.springframework.security.core.AuthenticationException;

public class InvalidCredentialsException extends  AuthenticationException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
