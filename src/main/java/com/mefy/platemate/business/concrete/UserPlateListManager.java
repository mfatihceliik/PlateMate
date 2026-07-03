package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.abstracts.IUserPlateListService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateAlarmDao;
import com.mefy.platemate.dataAccess.abstracts.ISavedPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateAlarm;
import com.mefy.platemate.entities.concrete.SavedPlate;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.MyPlateListsDto;
import com.mefy.platemate.entities.dto.PlateListItemDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPlateListManager implements IUserPlateListService {

    private final ISavedPlateDao savedPlateDao;
    private final IPlateAlarmDao plateAlarmDao;
    private final IUserDao userDao;
    private final IPlateSearchService plateSearchService;
    private final IAppSettingsService appSettingsService;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result savePlate(Long userId, String plateCode) {
        ResolvedPlate resolved = resolve(userId, plateCode);
        if (resolved.error != null) return resolved.error;

        if (!savedPlateDao.existsByUserIdAndPlateId(userId, resolved.plate.getId())) {
            SavedPlate savedPlate = new SavedPlate();
            savedPlate.setUser(resolved.user);
            savedPlate.setPlate(resolved.plate);
            savedPlateDao.save(savedPlate);
        }
        return new SuccessResult(messageService.getMessage(Messages.PLATE_SAVED_SUCCESS));
    }

    @Override
    @Transactional
    public Result unsavePlate(Long userId, String plateCode) {
        ResolvedPlate resolved = resolve(userId, plateCode);
        if (resolved.error != null) return resolved.error;

        savedPlateDao.findByUserIdAndPlateId(userId, resolved.plate.getId())
                .ifPresent(savedPlateDao::delete);
        return new SuccessResult(messageService.getMessage(Messages.PLATE_UNSAVED_SUCCESS));
    }

    @Override
    @Transactional
    public Result createAlarm(Long userId, String plateCode) {
        ResolvedPlate resolved = resolve(userId, plateCode);
        if (resolved.error != null) return resolved.error;

        if (plateAlarmDao.existsByUserIdAndPlateId(userId, resolved.plate.getId())) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_ALARM_ALREADY_EXISTS));
        }

        int nonPremiumAlarmLimit = appSettingsService.getInt(AppSettingKey.NON_PREMIUM_PLATE_ALARM_LIMIT);
        if (!resolved.user.isPremiumActive() && plateAlarmDao.countByUserId(userId) >= nonPremiumAlarmLimit) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_ALARM_LIMIT_REACHED, nonPremiumAlarmLimit));
        }

        PlateAlarm alarm = new PlateAlarm();
        alarm.setUser(resolved.user);
        alarm.setPlate(resolved.plate);
        plateAlarmDao.save(alarm);
        return new SuccessResult(messageService.getMessage(Messages.PLATE_ALARM_SUCCESS));
    }

    @Override
    @Transactional
    public Result removeAlarm(Long userId, String plateCode) {
        ResolvedPlate resolved = resolve(userId, plateCode);
        if (resolved.error != null) return resolved.error;

        PlateAlarm alarm = plateAlarmDao.findByUserIdAndPlateId(userId, resolved.plate.getId()).orElse(null);
        if (alarm == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_ALARM_NOT_FOUND));
        }
        plateAlarmDao.delete(alarm);
        return new SuccessResult(messageService.getMessage(Messages.PLATE_ALARM_REMOVED));
    }

    @Override
    public DataResult<MyPlateListsDto> getMyLists(Long userId) {
        List<PlateListItemDto> saved = savedPlateDao.findByUserIdWithPlate(userId).stream()
                .map(item -> toItemDto(item.getPlate(), item.getCreatedAt()))
                .toList();
        List<PlateListItemDto> alarms = plateAlarmDao.findByUserIdWithPlate(userId).stream()
                .map(item -> toItemDto(item.getPlate(), item.getCreatedAt()))
                .toList();
        return new SuccessDataResult<>(
                new MyPlateListsDto(saved, alarms),
                messageService.getMessage(Messages.PLATE_LISTS_FETCHED)
        );
    }

    private PlateListItemDto toItemDto(Plate plate, java.time.LocalDateTime createdAt) {
        return new PlateListItemDto(
                plate.getPlateCode(),
                plate.getCity() != null ? plate.getCity().getName() : null,
                plate.getRatingAverage(),
                plate.getReviewCount(),
                createdAt
        );
    }

    private ResolvedPlate resolve(Long userId, String plateCode) {
        String normalizedPlate = plateSearchService.normalizePlate(plateCode);

        Result validationResult = BusinessRules.run(plateSearchService.checkIfPlateValid(normalizedPlate));
        if (validationResult != null) return ResolvedPlate.error(validationResult);

        User user = userDao.findByIdAndActiveTrue(userId).orElse(null);
        if (user == null) {
            return ResolvedPlate.error(new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND)));
        }

        Plate plate = plateSearchService.getOrCreatePlate(normalizedPlate);
        return ResolvedPlate.ok(user, plate);
    }

    private static final class ResolvedPlate {
        private final User user;
        private final Plate plate;
        private final Result error;

        private ResolvedPlate(User user, Plate plate, Result error) {
            this.user = user;
            this.plate = plate;
            this.error = error;
        }

        static ResolvedPlate ok(User user, Plate plate) {
            return new ResolvedPlate(user, plate, null);
        }

        static ResolvedPlate error(Result error) {
            return new ResolvedPlate(null, null, error);
        }
    }
}
