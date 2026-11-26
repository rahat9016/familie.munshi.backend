package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class DuplicateBrandPositionException extends ConflictException {
    public DuplicateBrandPositionException(Object... params) {
        super("duplicateBrandPosition", params);
    }
}
