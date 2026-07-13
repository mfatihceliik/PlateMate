package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminMenuController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IAdminMenuService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.AdminMenuItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminMenuController implements IAdminMenuController {

    private final IAdminAccessService adminAccessService;
    private final IAdminMenuService adminMenuService;

    @Override
    public ResponseEntity<DataResult<List<AdminMenuItemDto>>> getMenu(
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<List<AdminMenuItemDto>> result = adminMenuService.getMenu();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
