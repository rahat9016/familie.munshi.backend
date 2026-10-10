/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] messageParams;

    protected BusinessException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageParams = new Object[0];
    }

    protected BusinessException(String messageKey, Object[] messageParams) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageParams = messageParams != null ? messageParams : new Object[0];
    }
}