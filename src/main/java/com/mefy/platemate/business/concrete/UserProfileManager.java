package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.business.abstracts.IUserReviewService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileManager implements IUserProfileService {

    private final IUserProfileDao userProfileDao;
    private final UserProfileMapper userProfileMapper;
    private final IMessageService messageService;
    private final IUserReviewService userReviewService;

    @Override
    public DataResult<UserProfileDto> getByUserId(Long userId, int page, int size) {
        UserProfile profile = userProfileDao.findById(userId).orElse(null);
        
        Result result = BusinessRules.run(checkIfProfileExists(profile));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        UserProfileDto dto = userProfileMapper.entityToDto(profile);

        var reviewsResult = userReviewService.getByTargetProfileId(userId, page, size);
        if (reviewsResult.isSuccess()) {
            dto.setReviews(reviewsResult.getData());
        }

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PROFILE_FOUND));
    }

    @Override
    public Result update(UserProfile userProfile) {
        UserProfile existingProfile = userProfileDao.findById(userProfile.getId()).orElse(null);
        
        Result result = BusinessRules.run(
                checkIfProfileExists(existingProfile),
                checkIfBioTooLong(userProfile.getBio())
        );
        if (result != null) return result;

        existingProfile.setFirstName(userProfile.getFirstName());
        existingProfile.setLastName(userProfile.getLastName());
        existingProfile.setBio(userProfile.getBio());

        userProfileDao.save(existingProfile);
        return new SuccessResult(messageService.getMessage(Messages.PROFILE_UPDATED));
    }

    /// ----- BUSINESS RULES -----

    private Result checkIfBioTooLong(String bio) {
        if (bio != null && bio.length() > 500) {
            return new ErrorResult(messageService.getMessage(Messages.PROFILE_BIO_TOO_LONG));
        }
        return new SuccessResult();
    }

    private Result checkIfProfileExists(UserProfile profile) {
        if (profile == null) {
            return new ErrorResult(messageService.getMessage(Messages.PROFILE_NOT_FOUND));
        }
        return new SuccessResult();
    }
}
