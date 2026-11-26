package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.ResourceNotFoundException;

public class BrandNotFoundException extends ResourceNotFoundException {
    public BrandNotFoundException(Object... params) {
        super("brandNotFound", params);
    }
}
