package com.mefy.platemate.core.utilities.pagination;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.exceptions.InvalidPaginationException;
import lombok.Getter;

@Getter
public class PaginationRequest {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private final int page;
    private final int size;

    private PaginationRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public static PaginationRequest of(Integer page, Integer size) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : size;

        if (resolvedPage < 0) {
            throw new InvalidPaginationException(Messages.PAGINATION_PAGE_INVALID);
        }
        if (resolvedSize <= 0) {
            throw new InvalidPaginationException(Messages.PAGINATION_SIZE_MIN_INVALID);
        }
        if (resolvedSize > MAX_SIZE) {
            throw new InvalidPaginationException(Messages.PAGINATION_SIZE_MAX_INVALID, MAX_SIZE);
        }

        return new PaginationRequest(resolvedPage, resolvedSize);
    }
}
