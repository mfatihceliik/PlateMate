package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Online presence of a user as seen by another user. {@code online} already
 * accounts for the reciprocal visibility rule: it is false when either side has
 * online visibility disabled.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresenceDto implements IDto {
    private Long userId;
    private boolean online;
}
