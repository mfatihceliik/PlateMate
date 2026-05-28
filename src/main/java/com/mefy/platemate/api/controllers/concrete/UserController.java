package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserAdminDto;
import com.mefy.platemate.entities.dto.request.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements IUserController {

    private final IUserService userService;
    private final IAdminAccessService adminAccessService;
    private final IMessageService messageService;

    @Override
    public ResponseEntity<DataResult<List<UserAdminDto>>> getAll(@RequestAttribute("userId") Long currentUserId) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        return ResponseEntity.ok(userService.getAllForAdmin());
    }

    @Override
    public ResponseEntity<DataResult<UserAdminDto>> getById(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<UserAdminDto> result = userService.getByIdForAdmin(id);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<UserAdminDto>> getByUsername(
            @RequestParam String username,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<UserAdminDto> result = userService.getByUsernameForAdmin(username);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> update(
            @PathVariable("userId") Long userId,
            @RequestAttribute("userId") Long tokenUserId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        if (!userId.equals(tokenUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResult(messageService.getMessage("auth.unauthorized")));
        }

        Result result = userService.update(userId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long tokenUserId
    ) {
        if (!id.equals(tokenUserId)) {
            Result authResult = adminAccessService.checkAdmin(tokenUserId);
            if (!authResult.isSuccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
            }
        }

        Result result = userService.delete(id);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
