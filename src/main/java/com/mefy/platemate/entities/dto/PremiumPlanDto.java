package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumPlanDto implements IDto {
    private Long id;
    private String period;
    private BigDecimal amount;
    private String currency;
    private Integer discountPercent;
    private Integer sortOrder;
}
