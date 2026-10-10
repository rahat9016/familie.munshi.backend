/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String messageKey, Object... messageParams) {
        super(messageKey, messageParams);
    }
}