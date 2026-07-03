package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.AppSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateAppSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/admin/settings")
public interface IAdminSettingsController {

    @GetMapping
    ResponseEntity<DataResult<AppSettingsDto>> getSettings(
            @RequestAttribute("userId") Long currentUserId
    );

    @PutMapping
    ResponseEntity<Result> updateSettings(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateAppSettingsRequest request
    );
}
