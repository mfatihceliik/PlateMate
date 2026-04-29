package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserSettingsController;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserSettingsController implements IUserSettingsController {

    private final IUserSettingsService userSettingsService;

    @Override
    public ResponseEntity<DataResult<UserSettingsDto>> getByUserId(Long currentUserId) {
        return ResponseEntity.ok(userSettingsService.getByUserId(currentUserId));
    }

    @Override
    public ResponseEntity<Result> update(Long currentUserId, UpdateSettingsRequest request) {
        Result result = userSettingsService.updateSettings(currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
