package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PaginationMapper;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileManager implements IUserProfileService {

    private final IUserProfileDao userProfileDao;
    private final IPlateReviewDao plateReviewDao;
    private final UserProfileMapper userProfileMapper;
    private final PlateReviewMapper plateReviewMapper;
    private final IMessageService messageService;

    @Override
    public DataResult<UserProfileDto> getByUserId(Long userId, PaginationRequest paginationRequest) {
        UserProfile profile = userProfileDao.findById(userId).orElse(null);
        
        Result result = BusinessRules.run(checkIfProfileExists(profile));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        UserProfileDto dto = userProfileMapper.entityToDto(profile);

        var pageable = PageRequest.of(
                paginationRequest.getPage(),
                paginationRequest.getSize(),
                Sort.by("createdAt").descending()
        );
        var plateReviews = plateReviewDao.findByUserId(userId, pageable).map(plateReviewMapper::entityToDto);
        long reviewCountLong = plateReviewDao.countByUserId(userId);
        int reviewCount = reviewCountLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) reviewCountLong;
        long totalRatingSum = plateReviewDao.sumRatingByUserId(userId);
        double ratingAverage = reviewCount > 0 ? (double) totalRatingSum / reviewCount : 0.0;

        dto.setPlateReviews(PaginationMapper.fromPage(plateReviews));
        dto.setReviewCount(reviewCount);
        dto.setTotalRatingSum(totalRatingSum);
        dto.setDriverRating(ratingAverage);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.PROFILE_FOUND));
    }

    /// ----- BUSINESS RULES -----

    private Result checkIfProfileExists(UserProfile profile) {
        if (profile == null) {
            return new ErrorResult(messageService.getMessage(Messages.PROFILE_NOT_FOUND));
        }
        return new SuccessResult();
    }
}
