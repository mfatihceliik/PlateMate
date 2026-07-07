package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.dto.AppSettingsDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppSettingsMapper implements IMapper<Map<AppSettingKey, Integer>, AppSettingsDto> {

    @Override
    public AppSettingsDto entityToDto(Map<AppSettingKey, Integer> entity) {
        if (entity == null) {
            return null;
        }
        return new AppSettingsDto(
                entity.getOrDefault(AppSettingKey.NON_PREMIUM_PLATE_FOLLOW_LIMIT, 5),
                entity.getOrDefault(AppSettingKey.NON_PREMIUM_PLATE_ALARM_LIMIT, 3),
                entity.getOrDefault(AppSettingKey.PRE_APPROVAL_MESSAGE_LIMIT, 3),
                entity.getOrDefault(AppSettingKey.COMMENT_REPORT_THRESHOLD, 3)
        );
    }

    @Override
    public Map<AppSettingKey, Integer> dtoToEntity(AppSettingsDto dto) {
        throw new UnsupportedOperationException("Mapping from AppSettingsDto to Map is not supported");
    }
}
