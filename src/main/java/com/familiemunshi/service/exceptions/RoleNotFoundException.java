package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class RoleNotFoundException  extends ResourceNotFoundException {
    public RoleNotFoundException(Object ...params){
        super("roleNotFound", params);
    }
}
