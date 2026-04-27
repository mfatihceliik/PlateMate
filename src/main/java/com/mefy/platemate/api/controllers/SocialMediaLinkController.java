package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.ISocialMediaLinkService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.SocialMediaLink;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.request.AddSocialLinkRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialLinkRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/social-links")
@RequiredArgsConstructor
public class SocialMediaLinkController {

    private final ISocialMediaLinkService socialMediaLinkService;

    @PostMapping
    public ResponseEntity<Result> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddSocialLinkRequest request
    ) {

        UserProfile profile = new UserProfile();
        profile.setId(currentUserId);

        SocialMediaLink link = new SocialMediaLink();
        link.setPlatform(request.getPlatform());
        link.setUrl(request.getUrl());
        link.setUserProfile(profile);

        Result result = socialMediaLinkService.add(link);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping
    public ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateSocialLinkRequest request
    ) {

        SocialMediaLink link = new SocialMediaLink();
        link.setId(request.getId());
        link.setPlatform(request.getPlatform());
        link.setUrl(request.getUrl());

        Result result = socialMediaLinkService.update(link, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result result = socialMediaLinkService.delete(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
