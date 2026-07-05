package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminThemeController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IThemeService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.AccentColorAdminDto;
import com.mefy.platemate.entities.dto.request.AccentColorRequest;
import com.mefy.platemate.entities.dto.request.UpdateThemeActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateThemeGridSizeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminThemeController implements IAdminThemeController {

    private final IAdminAccessService adminAccessService;
    private final IThemeService themeService;

    @Override
    public ResponseEntity<DataResult<List<AccentColorAdminDto>>> getAllColors(Long currentUserId) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<List<AccentColorAdminDto>> result = themeService.getAllColors();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<AccentColorAdminDto>> addColor(Long currentUserId, AccentColorRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<AccentColorAdminDto> result = themeService.addColor(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<AccentColorAdminDto>> updateColor(Long id, Long currentUserId, AccentColorRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<AccentColorAdminDto> result = themeService.updateColor(id, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> setColorActive(Long id, Long currentUserId, UpdateThemeActiveRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = themeService.setColorActive(id, request.getActive());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> updateGridSize(Long currentUserId, UpdateThemeGridSizeRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = themeService.updateGridSize(request.getGridSize());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
