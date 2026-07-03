package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.dto.AppSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateAppSettingsRequest;

import java.util.Map;

public interface IAppSettingsService {
    int getInt(AppSettingKey key);
    void setInt(AppSettingKey key, int value);
    Map<AppSettingKey, Integer> getAll();

    DataResult<AppSettingsDto> getSettings();
    Result updateSettings(UpdateAppSettingsRequest request);
}
