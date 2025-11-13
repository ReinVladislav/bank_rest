package com.example.bankcards.service;

import com.example.bankcards.dto.request.DepositRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.card.CardBalanceResponse;
import com.example.bankcards.dto.response.card.CardNumberResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CustomUserDetail;
import com.example.bankcards.service.implementation.CardOwnerServiceImpl;
import com.example.bankcards.util.components.CardNumberUtil;
import com.example.bankcards.util.enums.CardStatus;
import com.example.bankcards.util.mappers.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardOwnerServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardNumberUtil cardNumberUtil;

    @InjectMocks
    private CardOwnerServiceImpl cardOwnerService;

    private UserModel currentUser;
    private Card activeCard;
    private Card inactiveCard;
    private UUID cardId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardId = UUID.randomUUID();
        currentUser = UserModel.builder()
                .id(UUID.randomUUID())
                .username("user")
                .build();

        activeCard = Card.builder()
                .id(cardId)
                .owner(currentUser)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();

        inactiveCard = Card.builder()
                .id(UUID.randomUUID())
                .owner(currentUser)
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.valueOf(500))
                .build();

        // Настройка SecurityContext для текущего пользователя
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new CustomUserDetail(currentUser));
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }


    @Test
    void testDeposit_Success() {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(200));

        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.of(activeCard));

        CardBalanceResponse response = cardOwnerService.deposit(cardId, request);

        assertEquals(BigDecimal.valueOf(1200), response.getBalance());
        verify(cardRepository).save(activeCard);
    }

    @Test
    void testDeposit_CardNotFound_ThrowsException() {
        DepositRequest request = new DepositRequest();
        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.deposit(cardId, request));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testDeposit_CardNotActive_ThrowsException() {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));
        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.of(inactiveCard));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.deposit(cardId, request));

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testTransfer_Success() {
        UUID creditCardId = UUID.randomUUID();
        Card creditCard = Card.builder()
                .id(creditCardId)
                .owner(currentUser)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();

        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(creditCardId);
        request.setAmount(BigDecimal.valueOf(300));

        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.of(activeCard));
        when(cardRepository.findCardWithLock(creditCardId, currentUser, false)).thenReturn(Optional.of(creditCard));

        cardOwnerService.transfer(request);

        assertEquals(BigDecimal.valueOf(700), activeCard.getBalance());
        assertEquals(BigDecimal.valueOf(800), creditCard.getBalance());
        verify(cardRepository).save(activeCard);
        verify(cardRepository).save(creditCard);
    }

    @Test
    void testTransfer_SameDebitAndCreditCard_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(cardId);
        request.setAmount(BigDecimal.valueOf(100));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.transfer(request));

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testTransfer_DebitCardNotFound_ThrowsException() {
        UUID creditCardId = UUID.randomUUID();
        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(creditCardId);
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.transfer(request));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testTransfer_CreditCardNotFound_ThrowsException() {
        UUID creditCardId = UUID.randomUUID();
        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(creditCardId);
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.of(activeCard));
        when(cardRepository.findCardWithLock(creditCardId, currentUser, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.transfer(request));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testTransfer_NotEnoughFunds_ThrowsException() {
        UUID creditCardId = UUID.randomUUID();
        Card creditCard = Card.builder()
                .id(creditCardId)
                .owner(currentUser)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();

        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(creditCardId);
        request.setAmount(BigDecimal.valueOf(2000)); // больше чем баланс

        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.of(activeCard));
        when(cardRepository.findCardWithLock(creditCardId, currentUser, false)).thenReturn(Optional.of(creditCard));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.transfer(request));

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testTransfer_CardNotActive_ThrowsException() {
        UUID creditCardId = UUID.randomUUID();
        Card creditCard = Card.builder()
                .id(creditCardId)
                .owner(currentUser)
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.valueOf(500))
                .build();

        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(creditCardId);
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findCardWithLock(cardId, currentUser, false)).thenReturn(Optional.of(activeCard));
        when(cardRepository.findCardWithLock(creditCardId, currentUser, false)).thenReturn(Optional.of(creditCard));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.transfer(request));

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testBlockRequest_Success() {
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.of(activeCard));
        CardResponse response = new CardResponse();
        when(cardMapper.toResponse(activeCard, cardNumberUtil)).thenReturn(response);

        CardResponse result = cardOwnerService.blockRequest(cardId);

        assertTrue(activeCard.getBlockRequested());
        assertEquals(response, result);
        verify(cardRepository).save(activeCard);
    }

    @Test
    void testBlockRequest_CardNotFound_ThrowsException() {
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.blockRequest(cardId));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testBlockRequest_CardNotActive_ThrowsException() {
        inactiveCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.of(inactiveCard));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.blockRequest(cardId));

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testShowBalance_Success() {
        activeCard.setBalance(BigDecimal.valueOf(1234));
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.of(activeCard));

        CardBalanceResponse response = cardOwnerService.showBalance(cardId);

        assertEquals(BigDecimal.valueOf(1234), response.getBalance());
    }

    @Test
    void testShowBalance_CardNotFound_ThrowsException() {
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.showBalance(cardId));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testShowNumber_Success() {
        activeCard.setEncryptNumber("encryptedNumber");
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.of(activeCard));
        when(cardNumberUtil.decryptAndFormat("encryptedNumber")).thenReturn("1234 5678 9012 3456");

        CardNumberResponse response = cardOwnerService.showNumber(cardId);

        assertEquals("1234 5678 9012 3456", response.getCardNumber());
    }

    @Test
    void testShowNumber_CardNotFound_ThrowsException() {
        when(cardRepository.findByIdAndOwnerAndIsDeleted(cardId, currentUser, false))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardOwnerService.showNumber(cardId));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testGetAllMyCards_Success() {
        Page<Card> page = new PageImpl<>(List.of(activeCard));
        when(cardRepository.findByOwnerAndIsDeleted(currentUser, false, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(cardMapper.toResponse(activeCard, cardNumberUtil)).thenReturn(new CardResponse());

        var result = cardOwnerService.getAllMyCards(1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
    }

}

