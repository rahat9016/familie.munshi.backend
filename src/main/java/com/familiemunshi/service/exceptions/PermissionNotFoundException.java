package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class PermissionNotFoundException extends ResourceNotFoundException {
    public PermissionNotFoundException(Object ...params){
        super("permissionNotFound", params);
    }
}
