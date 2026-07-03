package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IAppSettingDao;
import com.mefy.platemate.entities.concrete.AppSetting;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.dto.AppSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateAppSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AppSettingsManager implements IAppSettingsService {

    private final IAppSettingDao appSettingDao;
    private final IMessageService messageService;

    // Compile-time defaults stay in application.properties; the DB overrides them at runtime.
    @Value("${platemate.follow.non-premium-plate-limit:5}")
    private int nonPremiumPlateFollowLimitDefault;

    @Value("${platemate.alarm.non-premium-plate-limit:3}")
    private int nonPremiumPlateAlarmLimitDefault;

    @Value("${platemate.messaging.pre-approval-message-limit:3}")
    private int preApprovalMessageLimitDefault;

    @Value("${moderation.comment-report-threshold:3}")
    private int commentReportThresholdDefault;

    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    @Override
    public int getInt(AppSettingKey key) {
        return cache.computeIfAbsent(key.getKey(), ignored -> loadFromStore(key));
    }

    @Override
    public void setInt(AppSettingKey key, int value) {
        appSettingDao.save(new AppSetting(key.getKey(), Integer.toString(value)));
        cache.put(key.getKey(), value);
    }

    @Override
    public Map<AppSettingKey, Integer> getAll() {
        Map<AppSettingKey, Integer> result = new EnumMap<>(AppSettingKey.class);
        for (AppSettingKey key : AppSettingKey.values()) {
            result.put(key, getInt(key));
        }
        return result;
    }

    @Override
    public DataResult<AppSettingsDto> getSettings() {
        AppSettingsDto dto = new AppSettingsDto(
                getInt(AppSettingKey.NON_PREMIUM_PLATE_FOLLOW_LIMIT),
                getInt(AppSettingKey.NON_PREMIUM_PLATE_ALARM_LIMIT),
                getInt(AppSettingKey.PRE_APPROVAL_MESSAGE_LIMIT),
                getInt(AppSettingKey.COMMENT_REPORT_THRESHOLD)
        );
        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    public Result updateSettings(UpdateAppSettingsRequest request) {
        if (request.getNonPremiumPlateFollowLimit() != null) {
            setInt(AppSettingKey.NON_PREMIUM_PLATE_FOLLOW_LIMIT, request.getNonPremiumPlateFollowLimit());
        }
        if (request.getNonPremiumPlateAlarmLimit() != null) {
            setInt(AppSettingKey.NON_PREMIUM_PLATE_ALARM_LIMIT, request.getNonPremiumPlateAlarmLimit());
        }
        if (request.getPreApprovalMessageLimit() != null) {
            setInt(AppSettingKey.PRE_APPROVAL_MESSAGE_LIMIT, request.getPreApprovalMessageLimit());
        }
        if (request.getCommentReportThreshold() != null) {
            setInt(AppSettingKey.COMMENT_REPORT_THRESHOLD, request.getCommentReportThreshold());
        }
        return new SuccessResult(messageService.getMessage(Messages.SETTINGS_UPDATED));
    }

    private int loadFromStore(AppSettingKey key) {
        return appSettingDao.findById(key.getKey())
                .map(setting -> parseOrDefault(setting.getValue(), defaultFor(key)))
                .orElse(defaultFor(key));
    }

    private int defaultFor(AppSettingKey key) {
        return switch (key) {
            case NON_PREMIUM_PLATE_FOLLOW_LIMIT -> nonPremiumPlateFollowLimitDefault;
            case NON_PREMIUM_PLATE_ALARM_LIMIT -> nonPremiumPlateAlarmLimitDefault;
            case PRE_APPROVAL_MESSAGE_LIMIT -> preApprovalMessageLimitDefault;
            case COMMENT_REPORT_THRESHOLD -> commentReportThresholdDefault;
        };
    }

    private int parseOrDefault(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
