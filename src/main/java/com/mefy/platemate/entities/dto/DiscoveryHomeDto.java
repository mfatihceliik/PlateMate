package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryHomeDto implements IDto {
    private DiscoveryDailyStatsDto dailyStats;
    private DiscoveryTabsDto tabs;
    private List<DiscoveryCityStatDto> cityStats;
    private List<CityPlateActivityDto> topCityPlates;
    private List<DiscoveryRecentActivityDto> recentActivities;
    private String feedType;
    private DiscoveryExtendedStatsDto extendedStats;
    private DiscoveryForYouDto forYou;
    private List<DiscoveryTabOptionDto> tabOptions;
}
