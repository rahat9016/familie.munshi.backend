package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.BusinessException;

public class InvalidRolePermissionAssignmentException
        extends BusinessException {

    public InvalidRolePermissionAssignmentException(
            Object... params
    ) {
        super("invalidRolePermissionAssignment", params);
    }
}
