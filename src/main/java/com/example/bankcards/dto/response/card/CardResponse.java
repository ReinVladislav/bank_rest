package com.example.bankcards.dto.response.card;

import com.example.bankcards.util.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CardResponse {
    @NotNull
    private UUID id;
    @NotNull
    @JsonProperty("masked_number")
    private String maskedNumber;
    @NotNull
    private CardStatus status;
    @NotNull
    @JsonProperty("block_requested")
    private boolean blockRequested;
    @NotNull
    @JsonProperty("expiration_date")
    private OffsetDateTime expirationDate;

}
