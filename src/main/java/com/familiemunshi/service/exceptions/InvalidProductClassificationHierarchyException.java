package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class InvalidProductClassificationHierarchyException extends ConflictException {
    public InvalidProductClassificationHierarchyException(Object ...params) {
        super("invalidProductClassificationHierarchy", params);
    }
}