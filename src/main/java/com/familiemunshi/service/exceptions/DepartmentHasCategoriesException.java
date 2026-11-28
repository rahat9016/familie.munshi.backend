package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ConflictException;

public class DepartmentHasCategoriesException extends ConflictException {
    public DepartmentHasCategoriesException(Object... params) {
        super("departmentHasCategories", params);
    }
}
