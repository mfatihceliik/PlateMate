package com.mefy.platemate.core.utilities.pagination;

import org.springframework.data.domain.Page;

public final class PaginationMapper {

    private PaginationMapper() {
    }

    public static <T> PagedData<T> fromPage(Page<T> page) {
        return new PagedData<>(
                page.getContent(),
                new PaginationMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        );
    }
}
