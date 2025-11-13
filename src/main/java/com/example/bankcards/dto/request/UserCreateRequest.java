package com.example.bankcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
public class UserCreateRequest {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @JsonProperty("repeat_password")
    @NotBlank(message = "Repeat password cannot be empty")
    private String repeatPassword;

}
