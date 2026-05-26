package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PlateRemovalRequestDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/plates")
public interface IPlateRemovalRequestController {

    @PostMapping("/{plateId}/removal-requests")
    ResponseEntity<DataResult<PlateRemovalRequestDto>> addRequest(
            @PathVariable Long plateId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPlateRemovalRequestRequest request
    );
}
