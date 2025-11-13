package com.example.bankcards.service.interfaces.card;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.CardWithOwnerResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;

import java.util.List;
import java.util.UUID;

public interface CardAdminService {

    void delete(UUID id);
    CardWithOwnerResponse activate(UUID id);
    CardWithOwnerResponse block(UUID id);
    CardWithOwnerResponse create(CreateCardRequest request);
    ListCardResponse<CardWithOwnerResponse> getAll(Boolean haveBlockRequest, int page, int size);

}
