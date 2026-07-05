package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumFeatureAdminDto implements IDto {
    private Long id;
    private String iconKey;
    private String titleTr;
    private String titleEn;
    private String subtitleTr;
    private String subtitleEn;
    private Integer sortOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
