package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLocationDto implements IDto {
    private Long id;
    private Long userId;
    private String username;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdatedAt;
}
