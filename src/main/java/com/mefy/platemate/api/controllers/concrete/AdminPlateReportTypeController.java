package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminPlateReportTypeController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IPlateReportTypeService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import com.mefy.platemate.entities.dto.request.AddPlateReportTypeRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminPlateReportTypeController implements IAdminPlateReportTypeController {

    private final IAdminAccessService adminAccessService;
    private final IPlateReportTypeService plateReportTypeService;

    @Override
    public ResponseEntity<DataResult<List<PlateReportTypeAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<List<PlateReportTypeAdminDto>> result = plateReportTypeService.getAllReportTypes();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PlateReportTypeAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPlateReportTypeRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<PlateReportTypeAdminDto> result = plateReportTypeService.addReportType(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<PlateReportTypeAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePlateReportTypeRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<PlateReportTypeAdminDto> result = plateReportTypeService.updateReportType(id, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePlateReportTypeActiveRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }

        Result result = plateReportTypeService.setReportTypeActive(id, request.getActive());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
