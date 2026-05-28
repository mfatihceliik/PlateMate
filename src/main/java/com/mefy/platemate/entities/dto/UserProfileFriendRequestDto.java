package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileFriendRequestDto implements IDto {
    private Long id;
    private Long requesterUserId;
    private String requesterUsername;
    private Long addresseeUserId;
    private String addresseeUsername;
    private Long statusId;
    private String statusCode;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime lastActionAt;
}
