package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IPlateRemovalRequestController;
import com.mefy.platemate.business.abstracts.IPlateRemovalRequestService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PlateRemovalRequestDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlateRemovalRequestController implements IPlateRemovalRequestController {

    private final IPlateRemovalRequestService plateRemovalRequestService;

    @Override
    public ResponseEntity<DataResult<PlateRemovalRequestDto>> addRequest(
            @PathVariable Long plateId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPlateRemovalRequestRequest request
    ) {
        DataResult<PlateRemovalRequestDto> result =
                plateRemovalRequestService.addRequest(plateId, currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
