package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.SocialPlatformDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/social-platforms")
public interface ISocialPlatformController {

    @GetMapping
    ResponseEntity<DataResult<List<SocialPlatformDto>>> getAllActive();
}
