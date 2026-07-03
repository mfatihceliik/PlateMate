package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlateTagSummaryDto implements IDto {
    private String code;
    private String label;
    private String iconKey;
    private String colorHex;
    private long count;
}
