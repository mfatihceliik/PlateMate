package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ISocialMediaLinkService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ISocialMediaLinkDao;
import com.mefy.platemate.entities.concrete.SocialMediaLink;
import com.mefy.platemate.entities.concrete.SocialPlatform;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.request.AddSocialLinkRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialMediaLinkManager implements ISocialMediaLinkService {
    private final ISocialMediaLinkDao socialMediaLinkDao;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result add(AddSocialLinkRequest request, Long currentUserId) {
        SocialPlatform platform = SocialPlatform.resolve(request.getPlatformId(), request.getPlatformCode());

        Result result = BusinessRules.run(
                checkIfPlatformProvided(platform),
                checkIfPlatformExists(currentUserId, platform)
        );

        if (result != null) return result;

        UserProfile profile = new UserProfile();
        profile.setId(currentUserId);

        SocialMediaLink link = new SocialMediaLink();
        link.setPlatform(platform);
        link.setUrl(request.getUrl());
        link.setUserProfile(profile);

        socialMediaLinkDao.save(link);
        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_LINK_ADDED));
    }

    private Result checkIfPlatformExists(Long userProfileId, SocialPlatform platform) {
        if (socialMediaLinkDao.existsByUserProfileIdAndPlatform(userProfileId, platform)) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_PLATFORM_ALREADY_EXISTS));
        }
        return new SuccessResult();
    }

    private Result checkIfPlatformProvided(SocialPlatform platform) {
        if (platform == null) {
            return new ErrorResult(messageService.getMessage(Messages.VALIDATION_SOCIAL_PLATFORM_NOTNULL));
        }
        return new SuccessResult();
    }

    @Override
    @Transactional
    public Result update(UpdateSocialLinkRequest request, Long currentUserId) {
        SocialMediaLink existingLink = socialMediaLinkDao.findById(request.getId()).orElse(null);
        SocialPlatform platform = SocialPlatform.resolve(request.getPlatformId(), request.getPlatformCode());
        
        Result result = BusinessRules.run(
                checkIfLinkExists(existingLink),
                checkIfUserAuthorizedForLink(existingLink, currentUserId),
                checkIfPlatformProvided(platform),
                checkPlatformUpdate(platform, existingLink, currentUserId)
        );
        if (result != null) return result;

        existingLink.setPlatform(platform);
        existingLink.setUrl(request.getUrl());
        socialMediaLinkDao.save(existingLink);

        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_LINK_UPDATED));
    }

    @Override
    @Transactional
    public Result delete(Long id, Long currentUserId) {
        SocialMediaLink link = socialMediaLinkDao.findById(id).orElse(null);
        
        Result result = BusinessRules.run(
                checkIfLinkExists(link),
                checkIfUserAuthorizedForLink(link, currentUserId)
        );
        if (result != null) return result;
        socialMediaLinkDao.deleteById(id);
        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_LINK_DELETED));
    }

    private Result checkIfLinkExists(SocialMediaLink link) {
        if (link == null) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_LINK_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfUserAuthorizedForLink(SocialMediaLink link, Long currentUserId) {
        if (link == null) return new SuccessResult();
        
        if (!link.getUserProfile().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_LINK_DELETE_UNAUTHORIZED));
        }
        return new SuccessResult();
    }

    private Result checkPlatformUpdate(SocialPlatform newPlatform, SocialMediaLink existingLink, Long currentUserId) {
        if (existingLink == null) return new SuccessResult();
        
        if (existingLink.getPlatform() != newPlatform) {
            return BusinessRules.run(checkIfPlatformExists(currentUserId, newPlatform));
        }
        return new SuccessResult();
    }
}
