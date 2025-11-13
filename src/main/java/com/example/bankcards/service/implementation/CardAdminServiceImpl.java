package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.CardWithOwnerResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.interfaces.card.CardAdminService;
import com.example.bankcards.util.components.CardNumberUtil;
import com.example.bankcards.util.constants.ErrorMessages;
import com.example.bankcards.util.enums.CardStatus;
import com.example.bankcards.util.enums.RoleType;
import com.example.bankcards.util.mappers.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardAdminServiceImpl implements CardAdminService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;
    private final CardNumberUtil cardNumberUtil;

    @Override
    public void delete(UUID id) {
        Card card = cardRepository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));
        card.setIsDeleted(true);
        cardRepository.save(card);
        log.info("Card id={} marked as deleted", id);
    }

    @Override
    public CardWithOwnerResponse activate(UUID id) {
        Card card = cardRepository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));
        card.setStatus(CardStatus.ACTIVE);
        card.setBlockRequested(false);
        cardRepository.save(card);
        log.info("Card id={} activated", id);
        return cardMapper.toResponseWithOwner(card, cardNumberUtil);
    }

    @Override
    public CardWithOwnerResponse block(UUID id) {
        Card card = cardRepository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Card id={} blocked", id);
        return cardMapper.toResponseWithOwner(card, cardNumberUtil);
    }

    @Override
    public CardWithOwnerResponse create(CreateCardRequest request) {
        UserModel user = userRepository.findByIdAndIsDeleted(request.getOwner(), false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.USER_NOT_FOUND));

        if (!user.getRole().getName().equals(RoleType.USER.getValue())) {
            log.warn("Cannot create card: user id={} is not a regular user", user.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.ADMIN_OWNER_CARD_ERROR);
        }

        String encryptCardNumber;
        do {
            encryptCardNumber = cardNumberUtil.getRandomEncryptedCardNumber();
        }
        while (cardRepository.existsByEncryptNumber(encryptCardNumber));

        Card card = Card.builder()
                .owner(user)
                .encryptNumber(encryptCardNumber)
                .build();
        card = cardRepository.save(card);
        log.info("Card id={} created for user id={}", card.getId(), user.getId());
        return cardMapper.toResponseWithOwner(card, cardNumberUtil);
    }

    @Override
    public ListCardResponse<CardWithOwnerResponse> getAll(Boolean haveBlockRequest, int page, int size) {
        Page<Card> cardPage;
        PageRequest pageRequest = PageRequest.of(page-1, size);

        if (haveBlockRequest != null) {
            cardPage = cardRepository.findByBlockRequestedAndIsDeleted(haveBlockRequest, false, pageRequest);
        } else {
            cardPage = cardRepository.findByIsDeleted(false, pageRequest);
        }
        return ListCardResponse.of(
                cardPage.getTotalElements(),
                cardPage.get()
                        .map(i -> cardMapper.toResponseWithOwner(i, cardNumberUtil))
                        .toList()
                );

    }

}
