package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialMediaLinkDto implements IDto {
    private Long id;
    private Long platformId;
    private String platformCode;
    private String url;

    public SocialPlatform getPlatform() {
        return SocialPlatform.resolve(platformId, platformCode);
    }

    public void setPlatform(SocialPlatform platform) {
        this.platformId = platform == null ? null : platform.getId();
        this.platformCode = platform == null ? null : platform.getCode();
    }
}
