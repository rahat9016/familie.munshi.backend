/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public class TokenSecurityException extends SystemException {

    public TokenSecurityException(String message) {
        super(message);
    }

    public TokenSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "TOKEN_SECURITY_ERROR";
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }
}
