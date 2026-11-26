package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class DuplicateProductClassificationPositionException extends ConflictException {
    public DuplicateProductClassificationPositionException(Object... params) {
        super("duplicateProductClassificationPosition", params);
    }
}
