package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Object ...params){
        super("userNotFound", params);
    }
}
