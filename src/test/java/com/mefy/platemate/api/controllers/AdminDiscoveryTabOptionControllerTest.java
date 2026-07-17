package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.AdminDiscoveryTabOptionController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IDiscoveryTabOptionService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionAdminDto;
import com.mefy.platemate.entities.dto.request.AddDiscoveryTabOptionRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionActiveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDiscoveryTabOptionControllerTest {

    @Test
    void getAllReturnsForbiddenWhenUserIsNotAdmin() {
        IAdminAccessService adminAccessService = mock(IAdminAccessService.class);
        IDiscoveryTabOptionService discoveryTabOptionService = mock(IDiscoveryTabOptionService.class);
        when(adminAccessService.checkAdmin(7L)).thenReturn(new ErrorResult("forbidden"));

        AdminDiscoveryTabOptionController controller = new AdminDiscoveryTabOptionController(adminAccessService, discoveryTabOptionService);
        ResponseEntity<DataResult<List<DiscoveryTabOptionAdminDto>>> response = controller.getAll(7L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void addReturnsCreatedWhenAdminAuthorized() {
        IAdminAccessService adminAccessService = mock(IAdminAccessService.class);
        IDiscoveryTabOptionService discoveryTabOptionService = mock(IDiscoveryTabOptionService.class);
        when(adminAccessService.checkAdmin(1L)).thenReturn(new SuccessResult());
        when(discoveryTabOptionService.addTabOption(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new SuccessDataResult<>(new DiscoveryTabOptionAdminDto(), "created"));

        AdminDiscoveryTabOptionController controller = new AdminDiscoveryTabOptionController(adminAccessService, discoveryTabOptionService);
        AddDiscoveryTabOptionRequest request = new AddDiscoveryTabOptionRequest("MOST_COMMENTED", "Most Commented", 4);
        ResponseEntity<DataResult<DiscoveryTabOptionAdminDto>> response = controller.add(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void setActiveReturnsForbiddenWhenNotAdmin() {
        IAdminAccessService adminAccessService = mock(IAdminAccessService.class);
        IDiscoveryTabOptionService discoveryTabOptionService = mock(IDiscoveryTabOptionService.class);
        when(adminAccessService.checkAdmin(4L)).thenReturn(new ErrorResult("forbidden"));

        AdminDiscoveryTabOptionController controller = new AdminDiscoveryTabOptionController(adminAccessService, discoveryTabOptionService);
        ResponseEntity<Result> response = controller.setActive(1L, 4L, new UpdateDiscoveryTabOptionActiveRequest(false));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
