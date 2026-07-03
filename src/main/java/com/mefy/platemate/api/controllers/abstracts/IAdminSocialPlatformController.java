package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.SocialPlatformAdminDto;
import com.mefy.platemate.entities.dto.request.AddSocialPlatformRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialPlatformActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialPlatformRequest;
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

@RequestMapping("/api/admin/social-platforms")
public interface IAdminSocialPlatformController {

    @GetMapping
    ResponseEntity<DataResult<List<SocialPlatformAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping
    ResponseEntity<DataResult<SocialPlatformAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddSocialPlatformRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<DataResult<SocialPlatformAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateSocialPlatformRequest request
    );

    @PatchMapping("/{id}/active")
    ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateSocialPlatformActiveRequest request
    );
}
