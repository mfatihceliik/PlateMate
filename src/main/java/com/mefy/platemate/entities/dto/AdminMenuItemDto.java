package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminMenuItemDto implements IDto {
    private String code;
    private String title;
    private String iconKey;
    private Integer sortOrder;
    /** Null when the entry has no pending-work badge. */
    private Long badgeCount;
}
