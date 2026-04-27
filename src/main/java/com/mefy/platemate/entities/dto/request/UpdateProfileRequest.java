package com.mefy.platemate.entities.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;

    @Size(max = 500, message = "{validation.profile.bio.size}")
    private String bio;
}
