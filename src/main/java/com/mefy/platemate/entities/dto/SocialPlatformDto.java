package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialPlatformDto implements IDto {
    private Long id;
    private String code;
    private String label;
    private String iconUrl;
    private String baseUrl;
    private String backgroundColorHex;
    private String iconTintColorHex;
    private Integer sortOrder;
}
