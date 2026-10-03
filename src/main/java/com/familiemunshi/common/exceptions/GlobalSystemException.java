/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public class GlobalSystemException extends SystemException {

    public GlobalSystemException(String message) {
        super(message);
    }

    public GlobalSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "GlobalSystemException";
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }
}