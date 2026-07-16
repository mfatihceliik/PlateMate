package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IPlateRemovalRequestReasonController;
import com.mefy.platemate.business.abstracts.IPlateRemovalRequestReasonService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlateRemovalRequestReasonController implements IPlateRemovalRequestReasonController {

    private final IPlateRemovalRequestReasonService plateRemovalRequestReasonService;

    @Override
    public ResponseEntity<DataResult<List<PlateRemovalRequestReasonDto>>> getActiveReasons() {
        return ResponseEntity.ok(plateRemovalRequestReasonService.getActiveReasons());
    }
}
