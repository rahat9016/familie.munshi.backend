package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class ColorwayImageNotFoundException extends ResourceNotFoundException {
    public ColorwayImageNotFoundException(Object... params) {
        super("colorwayImageNotFound", params);
    }
}
