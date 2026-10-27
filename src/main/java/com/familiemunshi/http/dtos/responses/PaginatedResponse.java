package com.familiemunshi.http.dtos.responses;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

public record PaginatedResponse<T>(
        boolean success,
        int status,
        String message,
        PaginationMeta meta,
        List<T> data
) {
    public static <T> PaginatedResponse<T> of(
            String message,
            Page<?> page,
            List<T> data
    ) {
        return new PaginatedResponse<>(
                true,
                HttpStatus.OK.value(),
                message,
                PaginationMeta.from(page),
                data
        );
    }
}
