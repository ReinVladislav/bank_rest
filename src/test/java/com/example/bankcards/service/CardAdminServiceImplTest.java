package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.card.CardWithOwnerResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.implementation.CardAdminServiceImpl;
import com.example.bankcards.util.components.CardNumberUtil;
import com.example.bankcards.util.enums.CardStatus;
import com.example.bankcards.util.enums.RoleType;
import com.example.bankcards.util.mappers.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardAdminServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardNumberUtil cardNumberUtil;

    @InjectMocks
    private CardAdminServiceImpl cardAdminService;

    private Card card;
    private UserModel user;
    private Role role;
    private UUID id;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        role = new Role();
        role.setName(RoleType.USER.getValue());

        user = UserModel.builder()
                .id(UUID.randomUUID())
                .username("user")
                .role(role)
                .isDeleted(false)
                .build();

        card = Card.builder()
                .id(id)
                .encryptNumber("encrypted")
                .owner(user)
                .isDeleted(false)
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Test
    void testDelete_Success() {
        when(cardRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(card));

        cardAdminService.delete(id);

        assertTrue(card.getIsDeleted());
        verify(cardRepository).save(card);
    }

    @Test
    void testDelete_NotFound_ThrowsException() {
        when(cardRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardAdminService.delete(id));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testActivate_Success() {
        when(cardRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(card));
        CardWithOwnerResponse response = new CardWithOwnerResponse();
        when(cardMapper.toResponseWithOwner(card, cardNumberUtil)).thenReturn(response);

        CardWithOwnerResponse result = cardAdminService.activate(id);

        assertEquals(response, result);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertFalse(card.getBlockRequested());
        verify(cardRepository).save(card);
    }

    @Test
    void testActivate_NotFound_ThrowsException() {
        when(cardRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardAdminService.activate(id));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testBlock_Success() {
        when(cardRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(card));
        CardWithOwnerResponse response = new CardWithOwnerResponse();
        when(cardMapper.toResponseWithOwner(card, cardNumberUtil)).thenReturn(response);

        CardWithOwnerResponse result = cardAdminService.block(id);

        assertEquals(response, result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void testBlock_NotFound_ThrowsException() {
        when(cardRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardAdminService.block(id));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testCreate_Success() {
        CreateCardRequest request = new CreateCardRequest();
        request.setOwner(user.getId());

        when(userRepository.findByIdAndIsDeleted(user.getId(), false)).thenReturn(Optional.of(user));
        when(cardNumberUtil.getRandomEncryptedCardNumber()).thenReturn("enc1");
        when(cardRepository.existsByEncryptNumber("enc1")).thenReturn(false);

        Card savedCard = Card.builder()
                .id(UUID.randomUUID())
                .encryptNumber("enc1")
                .owner(user)
                .build();
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardWithOwnerResponse response = new CardWithOwnerResponse();
        when(cardMapper.toResponseWithOwner(savedCard, cardNumberUtil)).thenReturn(response);

        CardWithOwnerResponse result = cardAdminService.create(request);

        assertEquals(response, result);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void testCreate_UserNotFound_ThrowsException() {
        CreateCardRequest request = new CreateCardRequest();
        request.setOwner(UUID.randomUUID());

        when(userRepository.findByIdAndIsDeleted(request.getOwner(), false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardAdminService.create(request));

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testCreate_AdminUser_ThrowsException() {
        CreateCardRequest request = new CreateCardRequest();
        request.setOwner(user.getId());

        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN.getValue());
        user.setRole(adminRole);

        when(userRepository.findByIdAndIsDeleted(user.getId(), false)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardAdminService.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testGetAll_WithBlockRequest() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByBlockRequestedAndIsDeleted(true, false, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(cardMapper.toResponseWithOwner(card, cardNumberUtil))
                .thenReturn(new CardWithOwnerResponse());

        var result = cardAdminService.getAll(true, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testGetAll_WithoutBlockRequest() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByIsDeleted(false, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(cardMapper.toResponseWithOwner(card, cardNumberUtil))
                .thenReturn(new CardWithOwnerResponse());

        var result = cardAdminService.getAll(null, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
    }
}

