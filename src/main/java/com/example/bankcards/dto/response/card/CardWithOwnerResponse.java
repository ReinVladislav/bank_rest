package com.example.bankcards.dto.response.card;

import com.example.bankcards.dto.response.UserResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CardWithOwnerResponse extends CardResponse {
    @NotNull
    private UserResponse owner;
}
