package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/plate-report-types")
public interface IPlateReportTypeController {

    @GetMapping
    ResponseEntity<DataResult<List<PlateReportTypeDto>>> getAllActive();
}
