package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminSettingsController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.AppSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateAppSettingsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminSettingsController implements IAdminSettingsController {

    private final IAdminAccessService adminAccessService;
    private final IAppSettingsService appSettingsService;

    @Override
    public ResponseEntity<DataResult<AppSettingsDto>> getSettings(
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }
        return ResponseEntity.ok(appSettingsService.getSettings());
    }

    @Override
    public ResponseEntity<Result> updateSettings(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateAppSettingsRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = appSettingsService.updateSettings(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
