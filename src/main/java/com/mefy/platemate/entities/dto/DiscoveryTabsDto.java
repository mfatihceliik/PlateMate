package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryTabsDto implements IDto {
    private List<DiscoveryPlateCardDto> trendPlates;
    private List<DiscoveryPlateCardDto> attentionPlates;
    private List<DiscoveryPlateCardDto> goodDriverPlates;
    private List<DiscoveryPlateCardDto> newPlates;
}
