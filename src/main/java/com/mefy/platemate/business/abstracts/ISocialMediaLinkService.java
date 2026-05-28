package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.AddSocialLinkRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialLinkRequest;

public interface ISocialMediaLinkService {
    Result add(AddSocialLinkRequest request, Long currentUserId);
    Result update(UpdateSocialLinkRequest request, Long currentUserId);
    Result delete(Long id, Long currentUserId); // Sadece profil sahibi silebilir
}
