package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSocialLinkRequest implements IDto {
    @NotNull(message = "{validation.social.platform.notnull}")
    private SocialPlatform platform;

    @NotBlank(message = "{validation.social.url.notblank}")
    private String url;
}
