package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class DepartmentNotFoundException extends ResourceNotFoundException {
    public DepartmentNotFoundException(Object... params) {
        super("departmentNotFound", params);
    }
}
