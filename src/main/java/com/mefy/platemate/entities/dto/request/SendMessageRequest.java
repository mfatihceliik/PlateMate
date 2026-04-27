package com.mefy.platemate.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    @NotNull(message = "{validation.chat.room.notnull}")
    private Long chatRoomId;

    @NotBlank(message = "{validation.chat.content.notblank}")
    private String content;
}
