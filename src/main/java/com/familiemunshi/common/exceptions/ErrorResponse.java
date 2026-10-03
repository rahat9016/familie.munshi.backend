/*
 * Copyright (c) 2026 FamilieMunshi. All rights reserved.
 * Author: Minhazur Rahman <minhazur>
 */
package com.familiemunshi.common.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        int status,
        String code,
        String message,
        String method,
        String path,
        List<FieldErrorDto> errors,
        Instant timestamp
) {
    public static ErrorResponse of(
            int status, String code, String message, String method, String path) {
        return new ErrorResponse(
                false, status, code, message, method, path, null, Instant.now());
    }

    public static ErrorResponse withFieldErrors(
            int status,
            String code,
            String message,
            String method,
            String path,
            List<FieldErrorDto> errors) {
        return new ErrorResponse(
                false, status, code, message, method, path, errors, Instant.now());
    }
}