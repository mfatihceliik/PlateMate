package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryForYouDto implements IDto {
    private List<DiscoveryPlateCardDto> followedPlates;
    private List<DiscoveryPlateCardDto> savedPlates;
    private List<DiscoveryRecentActivityDto> followedPlateActivities;
    private DiscoveryPremiumStatsDto premiumStats;
}
