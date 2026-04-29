package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserLocationService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.UserLocationMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.*;
import com.mefy.platemate.entities.concrete.*;
import com.mefy.platemate.entities.dto.UserLocationDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserLocationManager implements IUserLocationService {

    private final IUserLocationDao userLocationDao;
    private final IUserDao userDao;
    private final IUserSettingsDao userSettingsDao;
    private final ILocationBlockDao locationBlockDao;
    private final IFriendshipDao friendshipDao;
    private final UserLocationMapper userLocationMapper;
    private final IMessageService messageService;

    @Override
    public Result updateLocation(Long userId, Double latitude, Double longitude) {
        User user = userDao.findById(userId).orElse(null);
        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) return result;

        UserLocation userLocation = userLocationDao.findByUserId(userId).orElse(new UserLocation());
        if (userLocation.getId() == null) {
            userLocation.setUser(user);
        }

        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
        userLocation.setLastUpdatedAt(LocalDateTime.now());

        userLocationDao.save(userLocation);
        return new SuccessResult(messageService.getMessage(Messages.LOCATION_UPDATED));
    }

    @Override
    public DataResult<UserLocationDto> getLocationByUserId(Long userId) {
        UserLocation userLocation = userLocationDao.findByUserId(userId).orElse(null);
        Result result = BusinessRules.run(checkIfLocationExists(userLocation));
        if (result != null) return new ErrorDataResult<>(result.getMessage());

        return new SuccessDataResult<>(userLocationMapper.entityToDto(userLocation), messageService.getMessage(Messages.LOCATIONS_LISTED));
    }

    @Override
    public DataResult<List<UserLocationDto>> getVisibleLocationsForUser(Long viewerId) {
        List<UserLocation> allLocations = userLocationDao.findAll();
        
        List<UserLocationDto> visibleLocations = allLocations.stream()
                .filter(loc -> !loc.getUser().getId().equals(viewerId)) // Kendisini haritada görmesin (opsiyonel, client handle edebilir)
                .filter(loc -> isLocationVisibleToViewer(loc.getUser().getId(), viewerId))
                .map(userLocationMapper::entityToDto)
                .collect(Collectors.toList());

        return new SuccessDataResult<>(visibleLocations, messageService.getMessage(Messages.LOCATIONS_LISTED));
    }

    @Override
    @Transactional
    public Result blockUserFromLocation(Long userId, Long targetUserId) {
        if (locationBlockDao.existsByUserIdAndBlockedUserId(userId, targetUserId)) {
            return new SuccessResult(); // Zaten engelli
        }
        
        User user = userDao.findById(userId).orElseThrow();
        User target = userDao.findById(targetUserId).orElseThrow();
        
        LocationBlock block = new LocationBlock();
        block.setUser(user);
        block.setBlockedUser(target);
        locationBlockDao.save(block);
        
        return new SuccessResult(messageService.getMessage("location.blocked"));
    }

    @Override
    @Transactional
    public Result unblockUserFromLocation(Long userId, Long targetUserId) {
        locationBlockDao.deleteByUserIdAndBlockedUserId(userId, targetUserId);
        return new SuccessResult(messageService.getMessage("location.unblocked"));
    }

    @Override
    public DataResult<List<Long>> getBlockedUserIds(Long userId) {
        List<Long> blockedIds = locationBlockDao.findByUserId(userId).stream()
                .map(b -> b.getBlockedUser().getId())
                .collect(Collectors.toList());
        return new SuccessDataResult<>(blockedIds);
    }

    /// BUSINESS RULES

    private boolean isLocationVisibleToViewer(Long ownerId, Long viewerId) {
        // 1. Sahibi paylaşımı açmış mı?
        UserSettings settings = userSettingsDao.findByUserId(ownerId).orElse(null);
        if (settings == null || !settings.isLocationSharingEnabled()) {
            return false;
        }

        // 2. Görüntüleyen kişi engellenmiş mi?
        if (locationBlockDao.existsByUserIdAndBlockedUserId(ownerId, viewerId)) {
            return false;
        }

        // 3. Arkadaşlık kontrolü
        Friendship friendship = friendshipDao.findBetweenUsers(ownerId, viewerId).orElse(null);
        return friendship != null && friendship.getStatus() == FriendshipStatus.ACCEPTED;
    }

    private Result checkIfUserExists(User user) {
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfLocationExists(UserLocation location) {
        if (location == null) {
            return new ErrorResult(messageService.getMessage(Messages.LOCATION_NOT_FOUND));
        }
        return new SuccessResult();
    }
}
