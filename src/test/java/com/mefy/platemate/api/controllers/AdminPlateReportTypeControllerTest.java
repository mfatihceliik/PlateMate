package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.AdminPlateReportTypeController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IPlateReportTypeService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import com.mefy.platemate.entities.dto.request.AddPlateReportTypeRequest;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminPlateReportTypeControllerTest {

    @Test
    void getAllReturnsForbiddenWhenUserIsNotAdmin() {
        IAdminAccessService adminAccessService = mock(IAdminAccessService.class);
        IPlateReportTypeService plateReportTypeService = mock(IPlateReportTypeService.class);
        when(adminAccessService.checkAdmin(7L)).thenReturn(new ErrorResult("forbidden"));

        AdminPlateReportTypeController controller = new AdminPlateReportTypeController(adminAccessService, plateReportTypeService);
        ResponseEntity<DataResult<List<PlateReportTypeAdminDto>>> response = controller.getAll(7L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void addReturnsCreatedWhenAdminAuthorized() {
        IAdminAccessService adminAccessService = mock(IAdminAccessService.class);
        IPlateReportTypeService plateReportTypeService = mock(IPlateReportTypeService.class);
        when(adminAccessService.checkAdmin(1L)).thenReturn(new SuccessResult());
        when(plateReportTypeService.addReportType(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new SuccessDataResult<>(new PlateReportTypeAdminDto(), "created"));

        AdminPlateReportTypeController controller = new AdminPlateReportTypeController(adminAccessService, plateReportTypeService);
        AddPlateReportTypeRequest request = new AddPlateReportTypeRequest(
                "TEST",
                "Test",
                "Desc",
                "icon",
                PlateReportSeverity.YELLOW,
                "#F9A825",
                1,
                1
        );
        ResponseEntity<DataResult<PlateReportTypeAdminDto>> response = controller.add(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void setActiveReturnsForbiddenWhenNotAdmin() {
        IAdminAccessService adminAccessService = mock(IAdminAccessService.class);
        IPlateReportTypeService plateReportTypeService = mock(IPlateReportTypeService.class);
        when(adminAccessService.checkAdmin(4L)).thenReturn(new ErrorResult("forbidden"));

        AdminPlateReportTypeController controller = new AdminPlateReportTypeController(adminAccessService, plateReportTypeService);
        ResponseEntity<Result> response = controller.setActive(1L, 4L, new com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeActiveRequest(false));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
