package com.example.bankcards.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    @NotNull
    @JsonProperty("access_token")
    private String accessToken;
    @NotNull
    @JsonProperty("refresh_token")
    private String refreshToken;
}
