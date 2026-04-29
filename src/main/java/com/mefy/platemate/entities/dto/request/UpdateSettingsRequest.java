package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSettingsRequest implements IDto {
    private Boolean messagingEnabled;
    private Boolean locationSharingEnabled;
    private Boolean notificationsEnabled;
}
