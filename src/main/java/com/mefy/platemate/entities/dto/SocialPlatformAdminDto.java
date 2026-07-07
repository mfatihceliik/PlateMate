package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialPlatformAdminDto implements IDto {
    private Long id;
    private String code;
    private Map<String, String> labels;
    private String iconUrl;
    private String baseUrl;
    private String backgroundColorHex;
    private String iconTintColorHex;
    private Integer sortOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
