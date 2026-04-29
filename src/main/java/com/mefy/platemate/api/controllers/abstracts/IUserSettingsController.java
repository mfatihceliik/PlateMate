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

    @GetMapping
    ResponseEntity<DataResult<UserSettingsDto>> getByUserId(@RequestAttribute("userId") Long currentUserId);

    @PutMapping
    ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateSettingsRequest request
    );
}
