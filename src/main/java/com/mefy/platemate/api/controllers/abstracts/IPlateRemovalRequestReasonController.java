package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/plate-removal-reasons")
@Tag(name = "Plate Removal Request Reasons", description = "Public API for fetching active plate removal reasons")
public interface IPlateRemovalRequestReasonController {

    @GetMapping
    @Operation(summary = "Get active plate removal reasons", description = "Returns active plate removal reasons for users")
    ResponseEntity<DataResult<List<PlateRemovalRequestReasonDto>>> getActiveReasons();
}
