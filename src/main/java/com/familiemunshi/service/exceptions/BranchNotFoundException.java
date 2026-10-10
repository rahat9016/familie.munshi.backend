package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class BranchNotFoundException extends ResourceNotFoundException {
    public BranchNotFoundException(Object ...params){
        super("branchNotFound", params);
    }
}
