package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class CategoryAlreadyExistsException extends ConflictException {
    public CategoryAlreadyExistsException(Object... params) {
        super("categoryAlreadyExists", params);
    }
}
