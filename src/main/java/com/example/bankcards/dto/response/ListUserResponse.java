package com.example.bankcards.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUserResponse {
    @NotNull
    private List<UserResponse> items;
    @NotNull
    private Long total;
}
