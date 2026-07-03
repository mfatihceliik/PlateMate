package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.ISocialPlatformController;
import com.mefy.platemate.business.abstracts.ISocialPlatformService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.SocialPlatformDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SocialPlatformController implements ISocialPlatformController {

    private final ISocialPlatformService socialPlatformService;

    @Override
    public ResponseEntity<DataResult<List<SocialPlatformDto>>> getAllActive() {
        DataResult<List<SocialPlatformDto>> result = socialPlatformService.getActivePlatforms();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
