package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.SocialPlatformAdminDto;
import com.mefy.platemate.entities.dto.SocialPlatformDto;
import com.mefy.platemate.entities.dto.request.AddSocialPlatformRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialPlatformRequest;

import java.util.List;

public interface ISocialPlatformService {
    DataResult<List<SocialPlatformDto>> getActivePlatforms();

    DataResult<List<SocialPlatformAdminDto>> getAllPlatforms();

    DataResult<SocialPlatformAdminDto> addPlatform(AddSocialPlatformRequest request);

    DataResult<SocialPlatformAdminDto> updatePlatform(Long id, UpdateSocialPlatformRequest request);

    Result setPlatformActive(Long id, boolean active);
}
