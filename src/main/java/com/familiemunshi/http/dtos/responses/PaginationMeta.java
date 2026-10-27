package com.familiemunshi.http.dtos.responses;

import org.springframework.data.domain.Page;

public record PaginationMeta(
        int currentPage,
        int lastPage,
        int perPage,
        long totalRecords
) {
    public static PaginationMeta from(Page<?> page) {
        return new PaginationMeta(
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
