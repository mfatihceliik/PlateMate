package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.concrete.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipDto {
    private Long id;
    private Long friendUserId;         // Karşı tarafın user ID'si
    private String friendUsername;      // Karşı tarafın kullanıcı adı
    private String friendFirstName;    // Karşı tarafın adı
    private String friendLastName;     // Karşı tarafın soyadı
    private FriendshipStatus status;
    private LocalDateTime createdAt;
}
