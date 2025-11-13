package com.example.bankcards.dto.response.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Data
public class CardNumberResponse {
    @NotNull
    @JsonProperty("card_number")
    private String cardNumber;
}
