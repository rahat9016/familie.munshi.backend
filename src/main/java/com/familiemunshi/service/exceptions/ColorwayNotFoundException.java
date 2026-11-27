package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class ColorwayNotFoundException extends ResourceNotFoundException {
    public ColorwayNotFoundException(Object... params) {
        super("colorwayNotFound", params);
    }
}
