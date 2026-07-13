package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryTabFeedRequest implements IDto {
    private List<Integer> cityIds;
    private String reportTypeCode;
    private Double minRating;
    private Integer windowDays;

    public boolean hasPremiumFilters() {
        return reportTypeCode != null || windowDays != null;
    }
}
