package com.familiemunshi.service.exceptions;

import com.familiemunshi.common.exceptions.BadRequestException;

public class InvalidSeasonStatusException extends BadRequestException {
    public InvalidSeasonStatusException(Object... params) {
        super("invalidSeasonStatus", params);
    }
}
