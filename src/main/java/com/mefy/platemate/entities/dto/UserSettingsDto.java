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
    private boolean onlineVisibilityEnabled;
    private boolean messageNotificationsEnabled;
    private boolean friendNotificationsEnabled;
    private boolean plateReviewNotificationsEnabled;
    private boolean newFollowerNotificationsEnabled;
    private boolean reviewReplyNotificationsEnabled;
    private boolean followingListVisible;
}
