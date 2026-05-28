package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.PlateController;
import com.mefy.platemate.business.abstracts.IPlateService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.exceptions.GlobalExceptionHandler;
import com.mefy.platemate.core.exceptions.InvalidPaginationException;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaginationValidationUnitTest {

    @Test
    void plateControllerThrowsForNegativePage() {
        IPlateService plateService = mock(IPlateService.class);
        PlateController controller = new PlateController(plateService);

        assertThrows(
                InvalidPaginationException.class,
                () -> controller.getReviews("34ABC123", -1, 20)
        );
    }

    @Test
    void globalHandlerReturnsBadRequestForInvalidPagination() {
        IMessageService messageService = mock(IMessageService.class);
        when(messageService.getMessage(Messages.PAGINATION_INVALID)).thenReturn("Invalid pagination parameters.");
        when(messageService.getMessage(anyString(), org.mockito.ArgumentMatchers.<Object[]>any()))
                .thenReturn("Invalid pagination parameter.");

        GlobalExceptionHandler handler = new GlobalExceptionHandler(messageService);
        InvalidPaginationException exception = new InvalidPaginationException(Messages.PAGINATION_PAGE_INVALID);

        ResponseEntity<ErrorDataResult<String>> response = handler.handleInvalidPaginationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid pagination parameters.", response.getBody().getMessage());
    }
}
