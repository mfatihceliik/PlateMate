package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/settings")
public interface IUserSettingsController {

    @GetMapping("/{userId}")
    ResponseEntity<DataResult<UserSettingsDto>> getByUserId(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long tokenUserId);

    @PutMapping("/{userId}")
    ResponseEntity<Result> update(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long tokenUserId,
            @Valid @RequestBody UpdateSettingsRequest request
    );
}
