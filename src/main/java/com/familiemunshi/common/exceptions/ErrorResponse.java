/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        int status,
        String code,
        String message, List<FieldErrorDto> errors
) {

    public ErrorResponse {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(false, status, code, message, List.of());
    }

    public static ErrorResponse withFieldErrors(
            int status,
            String code,
            String message, List<FieldErrorDto> errors) {
        return new ErrorResponse(false, status, code, message, errors);
    }
}