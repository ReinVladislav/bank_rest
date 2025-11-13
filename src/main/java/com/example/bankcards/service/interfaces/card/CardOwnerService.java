package com.example.bankcards.service.interfaces.card;

import com.example.bankcards.dto.request.DepositRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.card.CardBalanceResponse;
import com.example.bankcards.dto.response.card.CardNumberResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CardOwnerService {
    void transfer(TransferRequest request);

    CardResponse blockRequest(UUID cardId);

    CardBalanceResponse showBalance(UUID cardId);

    CardNumberResponse showNumber(UUID cardId);

    ListCardResponse<CardResponse> getAllMyCards(int page, int size);

    CardBalanceResponse deposit(UUID cardId, DepositRequest request);

}
