package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryRecentActivityDto implements IDto {
    private String username;
    private String plateCode;
    private DiscoveryActivityActionType actionType;
    private LocalDateTime occurredAt;
    private Integer rating;
    private String comment;
    private String reportTypeCode;
    private String reportTypeLabel;
}
