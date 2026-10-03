/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public class StorageException extends SystemException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "STORAGE_ERROR";
    }

    @Override
    public Object[] getParams() {
        return new Object[0];
    }
}