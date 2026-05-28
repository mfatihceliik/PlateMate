package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsOverviewDto implements IDto {
    private String email;
    private boolean premiumActive;
    private LocalDateTime premiumUntil;
    private UserSettingsDto userSettings;
    private List<SocialMediaLinkDto> socialMediaLinks;
}
