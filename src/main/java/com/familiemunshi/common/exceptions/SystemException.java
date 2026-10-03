/*
 * Copyright (c) 2026 FamilyMunshi. All rights reserved.
 */
package com.familiemunshi.common.exceptions;

public abstract class SystemException extends RuntimeException {

    protected SystemException(String message) {
        super(message);
    }

    protected SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getCode();

    public abstract Object[] getParams();
}