package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFollowService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.rules.RelationshipRules;
import com.mefy.platemate.business.utilities.rules.UserRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFollowDao;
import com.mefy.platemate.entities.concrete.Follow;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.FollowListItemDto;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowManager implements IFollowService {

    private final IFollowDao followDao;
    private final INotificationService notificationService;
    private final IMessageService messageService;
    private final UserRules userRules;
    private final RelationshipRules relationshipRules;
    private final IUserSettingsService userSettingsService;

    @Override
    @Transactional
    public Result follow(Long followerId, Long followingId) {
        Result guard = BusinessRules.run(
                () -> relationshipRules.notSelf(followerId, followingId, Messages.FOLLOW_SELF_NOT_ALLOWED),
                () -> relationshipRules.notAlreadyExists(
                        followDao.existsByFollowerIdAndFollowingId(followerId, followingId),
                        Messages.FOLLOW_ALREADY_EXISTS));
        if (guard != null) {
            return guard;
        }

        DataResult<User> followerResult = userRules.resolveActiveUser(followerId);
        if (!followerResult.isSuccess()) {
            return new ErrorResult(followerResult.getMessage());
        }
        DataResult<User> followingResult = userRules.resolveActiveUser(followingId);
        if (!followingResult.isSuccess()) {
            return new ErrorResult(followingResult.getMessage());
        }
        User follower = followerResult.getData();
        User following = followingResult.getData();

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followDao.save(follow);

        String title = messageService.getMessage(Messages.NOTIFICATION_NEW_FOLLOWER_TITLE);
        String content = messageService.getMessage(Messages.NOTIFICATION_NEW_FOLLOWER_CONTENT, follower.getUsername());
        notificationService.sendNotification(followingId, title, content, NotificationType.NEW_FOLLOWER.name());

        return new SuccessResult(messageService.getMessage(Messages.FOLLOW_SUCCESS));
    }

    @Override
    @Transactional
    public Result unfollow(Long followerId, Long followingId) {
        Follow follow = followDao.findByFollowerIdAndFollowingId(followerId, followingId).orElse(null);
        if (follow == null) {
            return new ErrorResult(messageService.getMessage(Messages.FOLLOW_NOT_FOUND));
        }

        followDao.delete(follow);
        return new SuccessResult(messageService.getMessage(Messages.UNFOLLOW_SUCCESS));
    }

    @Override
    public long countFollowers(Long userId) {
        return followDao.countByFollowingId(userId);
    }

    @Override
    public long countFollowing(Long userId) {
        return followDao.countByFollowerId(userId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return followDao.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public DataResult<List<FollowListItemDto>> getFollowers(Long userId, Long requesterId) {
        List<FollowListItemDto> items = followDao.findByFollowingId(userId).stream()
                .map(row -> toFollowListItemDto(row.getFollower(), requesterId))
                .collect(Collectors.toList());
        return new SuccessDataResult<>(items, messageService.getMessage(Messages.USERS_LISTED));
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataResult<List<FollowListItemDto>> getFollowing(Long userId, Long requesterId) {
        boolean selfViewer = requesterId != null && requesterId.equals(userId);
        if (!selfViewer && !isFollowingListVisible(userId)) {
            return (DataResult<List<FollowListItemDto>>) (DataResult<?>) new ErrorDataResult<>(
                    Map.of("code", Messages.FOLLOW_FOLLOWING_LIST_HIDDEN),
                    messageService.getMessage(Messages.FOLLOW_FOLLOWING_LIST_HIDDEN)
            );
        }

        List<FollowListItemDto> items = followDao.findByFollowerId(userId).stream()
                .map(row -> toFollowListItemDto(row.getFollowing(), requesterId))
                .collect(Collectors.toList());
        return new SuccessDataResult<>(items, messageService.getMessage(Messages.USERS_LISTED));
    }

    private boolean isFollowingListVisible(Long userId) {
        DataResult<UserSettingsDto> settingsResult = userSettingsService.getByUserId(userId);
        return settingsResult.isSuccess()
                && settingsResult.getData() != null
                && settingsResult.getData().isFollowingListVisible();
    }

    private FollowListItemDto toFollowListItemDto(User user, Long requesterId) {
        FollowListItemDto dto = new FollowListItemDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        UserProfile profile = user.getProfile();
        if (profile != null) {
            dto.setDisplayName(profile.getDisplayName());
            dto.setBio(profile.getBio());
            dto.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        }

        dto.setIsFollowing(isFollowing(requesterId, user.getId()));
        return dto;
    }
}
