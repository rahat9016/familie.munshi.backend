package com.familiemunshi.http.dtos.responses;

public record SuccessResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> SuccessResponse<T> of(
            String message,
            T data
    ) {
        return new SuccessResponse<>(
                true,
                message,
                data
        );
    }
}