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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialMediaLinkManager implements ISocialMediaLinkService {
    private final ISocialMediaLinkDao socialMediaLinkDao;
    private final IMessageService messageService;

    @Override
    public Result add(SocialMediaLink link) {
        Result result = BusinessRules.run(
                checkIfPlatformExists(link.getUserProfile().getId(), link.getPlatform())
        );

        if (result != null) return result;

        socialMediaLinkDao.save(link);
        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_LINK_ADDED));
    }

    private Result checkIfPlatformExists(Long userProfileId, SocialPlatform platform) {
        if (socialMediaLinkDao.existsByUserProfileIdAndPlatform(userProfileId, platform)) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_PLATFORM_ALREADY_EXISTS));
        }
        return new SuccessResult();
    }

    @Override
    public Result update(SocialMediaLink link, Long currentUserId) {
        SocialMediaLink existingLink = socialMediaLinkDao.findById(link.getId()).orElse(null);
        if (existingLink == null) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_LINK_NOT_FOUND));
        }

        if (!existingLink.getUserProfile().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage("social.link.delete.unauthorized"));
        }

        // Eğer platform değişmişse, yeni platformun zaten olup olmadığını kontrol et
        if (existingLink.getPlatform() != link.getPlatform()) {
            Result result = BusinessRules.run(
                    checkIfPlatformExists(currentUserId, link.getPlatform())
            );
            if (result != null) return result;
        }

        existingLink.setPlatform(link.getPlatform());
        existingLink.setUrl(link.getUrl());
        socialMediaLinkDao.save(existingLink);

        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_LINK_UPDATED));
    }

    @Override
    public Result delete(Long id, Long currentUserId) {
        SocialMediaLink link = socialMediaLinkDao.findById(id).orElse(null);
        if (link == null) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_LINK_NOT_FOUND));
        }
        if (!link.getUserProfile().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage("social.link.delete.unauthorized"));
        }
        socialMediaLinkDao.deleteById(id);
        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_LINK_DELETED));
    }
}
