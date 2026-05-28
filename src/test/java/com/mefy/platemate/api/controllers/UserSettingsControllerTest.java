package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.UserSettingsController;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.entities.dto.UserSettingsOverviewDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSettingsControllerTest {

    @Test
    void getOverviewByUserIdReturnsForbiddenWhenNotSelf() {
        IUserSettingsService userSettingsService = mock(IUserSettingsService.class);
        IMessageService messageService = mock(IMessageService.class);
        when(messageService.getMessage("auth.unauthorized")).thenReturn("unauthorized");

        UserSettingsController controller = new UserSettingsController(userSettingsService, messageService);

        ResponseEntity<DataResult<UserSettingsOverviewDto>> response = controller.getOverviewByUserId(10L, 11L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("unauthorized", response.getBody().getMessage());
    }

    @Test
    void getOverviewByUserIdReturnsOkForSelf() {
        IUserSettingsService userSettingsService = mock(IUserSettingsService.class);
        IMessageService messageService = mock(IMessageService.class);
        UserSettingsOverviewDto dto = new UserSettingsOverviewDto();
        dto.setEmail("fatih@platemate.test");

        when(userSettingsService.getOverviewByUserId(10L)).thenReturn(new SuccessDataResult<>(dto, "settings-found"));

        UserSettingsController controller = new UserSettingsController(userSettingsService, messageService);

        ResponseEntity<DataResult<UserSettingsOverviewDto>> response = controller.getOverviewByUserId(10L, 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("fatih@platemate.test", response.getBody().getData().getEmail());
    }

    @Test
    void getOverviewByUserIdReturnsBadRequestWhenServiceFails() {
        IUserSettingsService userSettingsService = mock(IUserSettingsService.class);
        IMessageService messageService = mock(IMessageService.class);
        when(userSettingsService.getOverviewByUserId(10L)).thenReturn(new ErrorDataResult<>("user-not-found"));

        UserSettingsController controller = new UserSettingsController(userSettingsService, messageService);

        ResponseEntity<DataResult<UserSettingsOverviewDto>> response = controller.getOverviewByUserId(10L, 10L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("user-not-found", response.getBody().getMessage());
    }
}
