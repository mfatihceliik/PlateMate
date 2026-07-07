package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipDto implements IDto {
    private Long id;
    private Long friendUserId;
    private String friendUsername;
    private Long statusId;
    private String statusCode;
    private String statusLabel;
    private LocalDateTime createdAt;
}
