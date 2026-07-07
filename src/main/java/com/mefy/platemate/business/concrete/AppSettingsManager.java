package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.AppSettingsMapper;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AppSettingsManager implements IAppSettingsService {

    private final IAppSettingDao appSettingDao;
    private final IMessageService messageService;
    private final AppSettingsMapper appSettingsMapper;
    private final Environment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    @Override
    public int getInt(AppSettingKey key) {
        return cache.computeIfAbsent(key.getKey(), ignored -> loadFromStore(key));
    }

    @Override
    @Transactional
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
        AppSettingsDto dto = appSettingsMapper.entityToDto(getAll());
        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    @Transactional
    public Result updateSettings(UpdateAppSettingsRequest request) {
        Map<String, Object> requestMap = objectMapper.convertValue(request, new TypeReference<>() {});
        
        for (AppSettingKey key : AppSettingKey.values()) {
            Object value = requestMap.get(key.getDtoFieldName());
            if (value != null && value instanceof Integer intValue) {
                setInt(key, intValue);
            }
        }
        
        return new SuccessResult(messageService.getMessage(Messages.SETTINGS_UPDATED));
    }

    private int loadFromStore(AppSettingKey key) {
        int fallback = environment.getProperty(key.getKey(), Integer.class, key.getDefaultValue());
        return appSettingDao.findById(key.getKey())
                .map(setting -> parseOrDefault(setting.getValue(), fallback))
                .orElse(fallback);
    }

    private int parseOrDefault(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
