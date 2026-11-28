package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class SeasonAlreadyExistsException extends ConflictException {
    public SeasonAlreadyExistsException(Object... params) {
        super("seasonAlreadyExists", params);
    }
}
