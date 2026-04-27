package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.SocialMediaLink;

public interface ISocialMediaLinkService {
    Result add(SocialMediaLink link);
    Result update(SocialMediaLink link, Long currentUserId);
    Result delete(Long id, Long currentUserId); // Sadece profil sahibi silebilir
}
