package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements IDto {
    private Long id;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
    private LocalDateTime premiumUntil;
    private boolean premiumActive;
    private Long roleId;
    private String roleCode;
    private LocalDateTime currentSubscriptionStartedAt;
    private LocalDateTime currentSubscriptionExpiresAt;
    private Integer currentSubscriptionPurchasedDays;
    private Long currentSubscriptionStatusId;
    private String currentSubscriptionStatusCode;
    // Password yok!
}
