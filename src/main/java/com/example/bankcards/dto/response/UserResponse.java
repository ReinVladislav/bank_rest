package com.example.bankcards.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    @NotNull
    private UUID id;
    @NotNull
    private String username;
}
