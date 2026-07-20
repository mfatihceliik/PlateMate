package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowListItemDto implements IDto {
    private Long id;
    private String username;
    private String displayName;
    private String bio;
    private String profilePhotoUrl;
    private Boolean isFollowing;
}
