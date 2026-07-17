package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminDiscoveryTabOptionController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IDiscoveryTabOptionService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionAdminDto;
import com.mefy.platemate.entities.dto.request.AddDiscoveryTabOptionRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionRequest;
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
public class AdminDiscoveryTabOptionController implements IAdminDiscoveryTabOptionController {

    private final IAdminAccessService adminAccessService;
    private final IDiscoveryTabOptionService discoveryTabOptionService;

    @Override
    public ResponseEntity<DataResult<List<DiscoveryTabOptionAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<List<DiscoveryTabOptionAdminDto>> result = discoveryTabOptionService.getAllTabOptions();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<DiscoveryTabOptionAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddDiscoveryTabOptionRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<DiscoveryTabOptionAdminDto> result = discoveryTabOptionService.addTabOption(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<DiscoveryTabOptionAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateDiscoveryTabOptionRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<DiscoveryTabOptionAdminDto> result = discoveryTabOptionService.updateTabOption(id, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateDiscoveryTabOptionActiveRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }

        Result result = discoveryTabOptionService.setTabOptionActive(id, request.getActive());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
