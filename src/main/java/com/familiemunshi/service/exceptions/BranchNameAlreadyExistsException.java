package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class BranchNameAlreadyExistsException extends ConflictException {
    public BranchNameAlreadyExistsException(Object ...params){
        super("branchNameExists", params);
    }
}
