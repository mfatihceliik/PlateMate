package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserSettingsController;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.UserSettingsOverviewDto;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserSettingsController implements IUserSettingsController {

    private final IUserSettingsService userSettingsService;
    private final IMessageService messageService;

    @Override
    public ResponseEntity<DataResult<UserSettingsDto>> getByUserId(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long tokenUserId) {

        if (!userId.equals(tokenUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(messageService.getMessage(Messages.AUTH_UNAUTHORIZED)));
        }

        return ResponseEntity.ok(userSettingsService.getByUserId(userId));
    }

    @Override
    public ResponseEntity<DataResult<UserSettingsOverviewDto>> getOverviewByUserId(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long tokenUserId) {

        if (!userId.equals(tokenUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(messageService.getMessage(Messages.AUTH_UNAUTHORIZED)));
        }

        DataResult<UserSettingsOverviewDto> result = userSettingsService.getOverviewByUserId(userId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> update(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long tokenUserId,
            UpdateSettingsRequest request) {

        if (!userId.equals(tokenUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResult(messageService.getMessage(Messages.AUTH_UNAUTHORIZED)));
        }

        Result result = userSettingsService.updateSettings(userId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
