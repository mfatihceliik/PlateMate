package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSocialLinkRequest implements IDto {
    private Long platformId;

    private String platformCode;

    @NotBlank(message = "{validation.social.url.notblank}")
    private String url;
}
