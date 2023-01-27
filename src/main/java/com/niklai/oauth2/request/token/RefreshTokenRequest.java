package com.niklai.oauth2.request.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotEmpty
    @Pattern(regexp = "refresh_token")
    private String grantType;

    @NotEmpty
    private String refreshToken;
}
