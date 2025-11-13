package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.request.DepositRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.card.CardBalanceResponse;
import com.example.bankcards.dto.response.card.CardNumberResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CustomUserDetail;
import com.example.bankcards.service.interfaces.card.CardOwnerService;
import com.example.bankcards.util.components.CardNumberUtil;
import com.example.bankcards.util.constants.ErrorMessages;
import com.example.bankcards.util.enums.CardStatus;
import com.example.bankcards.util.mappers.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardOwnerServiceImpl implements CardOwnerService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardNumberUtil cardNumberUtil;

    @Override
    @Transactional
    public CardBalanceResponse deposit(UUID cardId, DepositRequest request) {
        UserModel user = getCurrentUser();
        Card card = cardRepository.findCardWithLock(cardId, user, false)
                .orElseThrow(() -> {
                    log.warn("Card id={} not found for user id={}", cardId, user.getId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND);
                });

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            log.warn("Attempt to deposit to non-active card id={} for user id={}", cardId, user.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.CARD_NOT_ACTIVE);
        }
        card.setBalance(
                card.getBalance().add(request.getAmount())
        );
        cardRepository.save(card);
        return new CardBalanceResponse(card.getBalance());
    }

    @Override
    @Transactional
    public void transfer(TransferRequest request) {
        if (Objects.equals(request.getDebitCardId(), request.getCreditCardId())) {
            log.warn("Transfer failed: debit and credit cards are the same id={}", request.getDebitCardId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.DEBIT_AND_CREDIT_CARD_ARE_SAME);
        }

        UserModel user = getCurrentUser();
        log.info("User id={} transferring from cardId={} to cardId={}",
                user.getId(), request.getDebitCardId(), request.getCreditCardId());

        Card debitCard = cardRepository.findCardWithLock(request.getDebitCardId(), user, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));
        Card creditCard = cardRepository.findCardWithLock(request.getCreditCardId(), user, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));

        if (!debitCard.getStatus().equals(CardStatus.ACTIVE) || !creditCard.getStatus().equals(CardStatus.ACTIVE)) {
            log.warn("One of the cards is not active");
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.CARD_NOT_ACTIVE);
        }

        BigDecimal amount = request.getAmount();
        if (debitCard.getBalance().compareTo(amount) < 0) {
            log.warn("Not enough funds on the debit card.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.NOT_ENOUGH_FUNDS);
        }

        debitCard.setBalance(
                debitCard.getBalance().subtract(amount)
        );
        creditCard.setBalance(
                creditCard.getBalance().add(amount)
        );
        cardRepository.save(debitCard);
        cardRepository.save(creditCard);
    }

    @Override
    public CardResponse blockRequest(UUID cardId) {
        UserModel user = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerAndIsDeleted(cardId, user, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            log.warn("Cannot request block: card id={} is not active", cardId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.CARD_NOT_ACTIVE);
        }
        card.setBlockRequested(true);
        cardRepository.save(card);
        log.info("Block request submitted for cardId={}", cardId);
        return cardMapper.toResponse(card, cardNumberUtil);
    }

    @Override
    public CardBalanceResponse showBalance(UUID cardId) {
        UserModel user = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerAndIsDeleted(cardId, user, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));
        return new CardBalanceResponse(card.getBalance());
    }

    @Override
    public CardNumberResponse showNumber(UUID cardId) {
        UserModel user = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerAndIsDeleted(cardId, user, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.CARD_NOT_FOUND));
        return new CardNumberResponse(
                cardNumberUtil.decryptAndFormat(card.getEncryptNumber())
        );
    }

    @Override
    public ListCardResponse<CardResponse> getAllMyCards(int page, int size) {
        UserModel user = getCurrentUser();
        Page<Card> cardsPage = cardRepository.findByOwnerAndIsDeleted(
                user,
                false,
                PageRequest.of(page - 1, size)
        );
        return ListCardResponse.of(
                cardsPage.getTotalElements(),
                cardsPage.get()
                        .map(i -> cardMapper.toResponse(i, cardNumberUtil))
                        .toList()
        );
    }

    private UserModel getCurrentUser() {
        return (
                (CustomUserDetail) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        ).getUser();
    }

}
