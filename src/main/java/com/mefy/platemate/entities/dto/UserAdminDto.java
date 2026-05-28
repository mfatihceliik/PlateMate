package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDto implements IDto {
    private Long id;
    private String username;
    private String email;
    private Long roleId;
    private String roleCode;
    private boolean premiumActive;
    private LocalDateTime premiumUntil;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
