package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class BrandTitleAlreadyExistsException extends ConflictException {
    public BrandTitleAlreadyExistsException(Object... params) {
        super("brandTitleExists", params);
    }
}
