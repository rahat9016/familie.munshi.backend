package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class RoleAlreadyExistsException extends ConflictException {
    public RoleAlreadyExistsException(Object ...params) {
        super("roleAlreadyExists", params);
    }
}
