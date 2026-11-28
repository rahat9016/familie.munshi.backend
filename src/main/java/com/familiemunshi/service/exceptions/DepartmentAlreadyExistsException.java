package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class DepartmentAlreadyExistsException extends ConflictException {
    public DepartmentAlreadyExistsException(Object... params) {
        super("departmentAlreadyExists", params);
    }
}
