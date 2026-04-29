package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsDto implements IDto {
    private boolean messagingEnabled;
    private boolean locationSharingEnabled;
    private boolean messageNotificationsEnabled;
    private boolean friendNotificationsEnabled;
}
