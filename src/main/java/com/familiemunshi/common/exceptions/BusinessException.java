/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public abstract class BusinessException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] messageParams;

    protected BusinessException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageParams = new Object[0];
    }

    protected BusinessException(String messageKey, Object... messageParams) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageParams = messageParams;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    public Object[] getMessageParams() {
        return this.messageParams;
    }
}