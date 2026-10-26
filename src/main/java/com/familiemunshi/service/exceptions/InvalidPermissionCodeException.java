package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.BusinessException;

public class InvalidPermissionCodeException extends BusinessException {
    public InvalidPermissionCodeException(Object... params) {
        super("invalidPermissionCode", params);
    }
}