package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class UserEmailAlreadyExistsException extends ConflictException {
    public UserEmailAlreadyExistsException(Object ...params){
        super("userEmailExists", params);
    }
}
