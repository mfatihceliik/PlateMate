package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IPlateReportTypeController;
import com.mefy.platemate.business.abstracts.IPlateReportTypeService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlateReportTypeController implements IPlateReportTypeController {

    private final IPlateReportTypeService plateReportTypeService;

    @Override
    public ResponseEntity<DataResult<List<PlateReportTypeDto>>> getAllActive() {
        DataResult<List<PlateReportTypeDto>> result = plateReportTypeService.getActiveReportTypes();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
