package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequest {

    @NotNull
    @DecimalMin(value = "1.00", message = "Minimum deposit amount is 1.00")
    @DecimalMax(value = "1000000.00", message = "Maximum deposit amount is 1,000,000.00")
    @Digits(integer = 7, fraction = 2, message = "Amount must have up to 2 decimal places")
    private BigDecimal amount;
}

