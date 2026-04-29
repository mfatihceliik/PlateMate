package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSignalDto implements IDto {
    private String title;
    private String content;
    private NotificationType type;
    private long timestamp;
}
