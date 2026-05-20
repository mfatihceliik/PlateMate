package com.mefy.platemate.core.utilities.pagination;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.exceptions.InvalidPaginationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationRequestTest {

    @Test
    void ofReturnsDefaultsWhenInputIsNull() {
        PaginationRequest request = PaginationRequest.of(null, null);

        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
    }

    @Test
    void ofThrowsWhenPageIsNegative() {
        InvalidPaginationException exception = assertThrows(
                InvalidPaginationException.class,
                () -> PaginationRequest.of(-1, 20)
        );

        assertEquals(Messages.PAGINATION_PAGE_INVALID, exception.getMessageKey());
    }

    @Test
    void ofThrowsWhenSizeIsNotPositive() {
        InvalidPaginationException exception = assertThrows(
                InvalidPaginationException.class,
                () -> PaginationRequest.of(0, 0)
        );

        assertEquals(Messages.PAGINATION_SIZE_MIN_INVALID, exception.getMessageKey());
    }

    @Test
    void ofThrowsWhenSizeExceedsMaximum() {
        InvalidPaginationException exception = assertThrows(
                InvalidPaginationException.class,
                () -> PaginationRequest.of(0, 101)
        );

        assertEquals(Messages.PAGINATION_SIZE_MAX_INVALID, exception.getMessageKey());
    }
}
