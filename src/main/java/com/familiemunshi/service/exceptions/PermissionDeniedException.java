package com.familiemunshi.service.exceptions;

import org.springframework.security.access.AccessDeniedException;

public class PermissionDeniedException extends AccessDeniedException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}