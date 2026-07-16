package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonAdminDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateRemovalRequestReasonActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateRemovalRequestReasonRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/admin/plate-removal-reasons")
@Tag(name = "Admin Plate Removal Request Reasons", description = "Admin API for managing plate removal request reasons")
public interface IAdminPlateRemovalRequestReasonController {

    @GetMapping
    @Operation(summary = "Get all plate removal reasons", description = "Returns all plate removal reasons (active and inactive)")
    ResponseEntity<DataResult<List<PlateRemovalRequestReasonAdminDto>>> getAllReasons();

    @PostMapping
    @Operation(summary = "Add plate removal reason", description = "Adds a new plate removal reason")
    ResponseEntity<DataResult<PlateRemovalRequestReasonAdminDto>> addReason(@RequestBody AddPlateRemovalRequestReasonRequest request);

    @PutMapping("/{id}")
    @Operation(summary = "Update plate removal reason", description = "Updates an existing plate removal reason")
    ResponseEntity<DataResult<PlateRemovalRequestReasonAdminDto>> updateReason(
            @PathVariable Long id,
            @RequestBody UpdatePlateRemovalRequestReasonRequest request
    );

    @PatchMapping("/{id}/active")
    @Operation(summary = "Set plate removal reason active status", description = "Activates or deactivates a plate removal reason")
    ResponseEntity<Result> setReasonActive(
            @PathVariable Long id,
            @RequestBody UpdatePlateRemovalRequestReasonActiveRequest request
    );
}
