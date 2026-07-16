package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminPlateRemovalRequestReasonController;
import com.mefy.platemate.business.abstracts.IPlateRemovalRequestReasonService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonAdminDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateRemovalRequestReasonActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateRemovalRequestReasonRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminPlateRemovalRequestReasonController implements IAdminPlateRemovalRequestReasonController {

    private final IPlateRemovalRequestReasonService plateRemovalRequestReasonService;

    @Override
    public ResponseEntity<DataResult<List<PlateRemovalRequestReasonAdminDto>>> getAllReasons() {
        return ResponseEntity.ok(plateRemovalRequestReasonService.getAllReasons());
    }

    @Override
    public ResponseEntity<DataResult<PlateRemovalRequestReasonAdminDto>> addReason(@Valid AddPlateRemovalRequestReasonRequest request) {
        return ResponseEntity.ok(plateRemovalRequestReasonService.addReason(request));
    }

    @Override
    public ResponseEntity<DataResult<PlateRemovalRequestReasonAdminDto>> updateReason(Long id, @Valid UpdatePlateRemovalRequestReasonRequest request) {
        return ResponseEntity.ok(plateRemovalRequestReasonService.updateReason(id, request));
    }

    @Override
    public ResponseEntity<Result> setReasonActive(Long id, @Valid UpdatePlateRemovalRequestReasonActiveRequest request) {
        return ResponseEntity.ok(plateRemovalRequestReasonService.setReasonActive(id, request.getActive()));
    }
}
