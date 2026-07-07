package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePremiumPlanRequest implements IDto {
    @NotNull(message = "{validation.premium.title.notblank}")
    private Map<String, String> titles;

    private Map<String, String> descriptions;

    @NotNull(message = "{validation.premium.amount.notnull}")
    @DecimalMin(value = "0.0", message = "{validation.premium.amount.min}")
    private BigDecimal amount;

    @NotBlank(message = "{validation.premium.currency.invalid}")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "{validation.premium.currency.invalid}")
    private String currency;

    @Min(value = 0, message = "{validation.premium.discount.range}")
    @Max(value = 100, message = "{validation.premium.discount.range}")
    private Integer discountPercent;

    @NotNull(message = "{validation.premium.sort.notnull}")
    @Min(value = 0, message = "{validation.premium.sort.min}")
    private Integer sortOrder;
}
