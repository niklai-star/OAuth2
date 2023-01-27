package com.niklai.oauth2.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class TokenInfo {

    @JsonProperty("access_type")
    @Builder.Default
    private String type = "Bearer";

    @JsonProperty("access_token")
    private String AccessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("re_expires_in")
    private Long refreshExpiresIn;

    public TokenInfo() {
        this.type = "Bearer";
    }
}
