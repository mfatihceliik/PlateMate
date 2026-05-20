package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import com.mefy.platemate.entities.dto.request.AddPlateReportTypeRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/plate-report-types")
public interface IAdminPlateReportTypeController {

    @GetMapping
    ResponseEntity<DataResult<List<PlateReportTypeAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping
    ResponseEntity<DataResult<PlateReportTypeAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPlateReportTypeRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<DataResult<PlateReportTypeAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePlateReportTypeRequest request
    );

    @PatchMapping("/{id}/active")
    ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePlateReportTypeActiveRequest request
    );
}
