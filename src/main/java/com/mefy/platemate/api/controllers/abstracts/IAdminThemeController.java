package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.AccentColorAdminDto;
import com.mefy.platemate.entities.dto.request.AccentColorRequest;
import com.mefy.platemate.entities.dto.request.UpdateThemeActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateThemeGridSizeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/theme")
public interface IAdminThemeController {

    @GetMapping("/colors")
    ResponseEntity<DataResult<List<AccentColorAdminDto>>> getAllColors(
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping("/colors")
    ResponseEntity<DataResult<AccentColorAdminDto>> addColor(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AccentColorRequest request
    );

    @PutMapping("/colors/{id}")
    ResponseEntity<DataResult<AccentColorAdminDto>> updateColor(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AccentColorRequest request
    );

    @PatchMapping("/colors/{id}/active")
    ResponseEntity<Result> setColorActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateThemeActiveRequest request
    );

    @PutMapping("/grid-size")
    ResponseEntity<Result> updateGridSize(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateThemeGridSizeRequest request
    );
}
