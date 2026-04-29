package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.AddSocialLinkRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialLinkRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/social-links")
public interface ISocialMediaLinkController {

    @PostMapping
    ResponseEntity<Result> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddSocialLinkRequest request
    );

    @PutMapping
    ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateSocialLinkRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );
}
