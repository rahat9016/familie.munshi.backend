package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.BadRequestException;

public class InvalidUserCreationException extends BadRequestException {

    public InvalidUserCreationException(Object... params) {
        super("invalidUserCreation", params);
    }
}
