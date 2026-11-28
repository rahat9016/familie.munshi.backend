package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class SeasonNotFoundException extends ResourceNotFoundException {
    public SeasonNotFoundException(Object... params) {
        super("seasonNotFound", params);
    }
}
