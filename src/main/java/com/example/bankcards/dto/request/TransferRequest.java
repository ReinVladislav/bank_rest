package com.example.bankcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @NotNull
    @JsonProperty("debit_card_id")
    private UUID debitCardId;
    @NotNull
    @JsonProperty("credit_card_id")
    private UUID creditCardId;
    @NotNull
    @DecimalMin(value = "1.00", message = "Transfer amount must be at least 1.00")
    @DecimalMax(value = "1000000.00", message = "Transfer amount must not exceed 1,000,000.00")
    @Digits(integer = 7, fraction = 2, message = "Transfer amount must have up to 2 decimal places")
    private BigDecimal amount;
}
