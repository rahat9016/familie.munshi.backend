/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public class ConflictException extends BusinessException {
    public ConflictException(String messageKey, Object[] messageParams) {
        super(messageKey, messageParams);
    }
}