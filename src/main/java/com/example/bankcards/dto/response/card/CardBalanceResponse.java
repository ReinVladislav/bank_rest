package com.example.bankcards.dto.response.card;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class CardBalanceResponse {
    @NotNull
    private BigDecimal balance;
}
