package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingsDto implements IDto {
    private Integer nonPremiumPlateFollowLimit;
    private Integer nonPremiumPlateAlarmLimit;
    private Integer preApprovalMessageLimit;
    private Integer commentReportThreshold;
}
