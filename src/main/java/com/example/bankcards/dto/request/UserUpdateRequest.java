package com.example.bankcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Username cannot be empty")
    private String username;
    private String password;
    @JsonProperty("repeat_password")
    private String repeatPassword;
}
