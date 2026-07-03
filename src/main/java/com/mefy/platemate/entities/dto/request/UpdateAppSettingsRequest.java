package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppSettingsRequest implements IDto {

    @Min(value = 1, message = "{validation.app.settings.min}")
    private Integer nonPremiumPlateFollowLimit;

    @Min(value = 1, message = "{validation.app.settings.min}")
    private Integer nonPremiumPlateAlarmLimit;

    @Min(value = 1, message = "{validation.app.settings.min}")
    private Integer preApprovalMessageLimit;

    @Min(value = 1, message = "{validation.app.settings.min}")
    private Integer commentReportThreshold;
}
