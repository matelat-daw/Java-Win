package com.asociaciondomitila.projects.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int currentPage,
        long totalItems,
        int totalPages,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}