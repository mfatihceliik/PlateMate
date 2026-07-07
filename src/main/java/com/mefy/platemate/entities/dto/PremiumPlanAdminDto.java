package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumPlanAdminDto implements IDto {
    private Long id;
    private String period;
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private BigDecimal amount;
    private String currency;
    private Integer discountPercent;
    private Integer sortOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
