/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public class DatabaseException extends SystemException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "DATABASE_ERROR";
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }
}