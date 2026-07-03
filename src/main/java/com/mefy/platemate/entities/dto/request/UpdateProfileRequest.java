package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest implements IDto {

    @Size(max = 50, message = "{validation.profile.displayname.max}")
    private String displayName;

    @Size(min = 3, max = 30, message = "{validation.user.username.size}")
    private String username;

    @Size(max = 160, message = "{validation.profile.bio.max}")
    private String bio;

    @Size(max = 500, message = "{validation.profile.photo.url.max}")
    private String profilePhotoUrl;
}
