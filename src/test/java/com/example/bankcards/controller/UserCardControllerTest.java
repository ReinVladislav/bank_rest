package com.example.bankcards.controller;

import com.example.bankcards.TestConfig;
import com.example.bankcards.UserCardControllerConfig;
import com.example.bankcards.dto.request.DepositRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.card.CardBalanceResponse;
import com.example.bankcards.dto.response.card.CardNumberResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;
import com.example.bankcards.service.interfaces.card.CardOwnerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserCardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, UserCardControllerConfig.class})
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardOwnerService service;
    @Autowired
    private ObjectMapper objectMapper;
    private UUID cardId;
    private CardBalanceResponse balanceResponse;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        balanceResponse = new CardBalanceResponse(BigDecimal.valueOf(1000));
        cardResponse = new CardResponse();
        reset(service);
    }

    @Test
    void testCheckBalance_Success() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardBalanceResponse balanceResponse = new CardBalanceResponse(BigDecimal.valueOf(1000));
        when(service.showBalance(any(UUID.class))).thenReturn(balanceResponse);

        mockMvc.perform(get("/cards/{id}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void testGetMyCards_Success() throws Exception {
        ListCardResponse<CardResponse> listResponse = ListCardResponse.of(1L, List.of(cardResponse));
        when(service.getAllMyCards(1, 10)).thenReturn(listResponse);

        mockMvc.perform(get("/cards")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0]").exists());

    }

    @Test
    void testTransfer_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setDebitCardId(cardId);
        request.setCreditCardId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(200));

        mockMvc.perform(post("/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(service).transfer(any(TransferRequest.class));
    }

    @Test
    void testDeposit_Success() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(500));

        when(service.deposit(cardId, request)).thenReturn(balanceResponse);

        mockMvc.perform(patch("/cards/{id}/deposit", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void testBlockCardRequest_Success() throws Exception {
        when(service.blockRequest(cardId)).thenReturn(cardResponse);

        mockMvc.perform((RequestBuilder) patch("/cards/{id}/block", cardId))
                .andExpect(status().isOk());
    }



    @Test
    void testShowCardNumber_Success() throws Exception {
        CardNumberResponse numberResponse = new CardNumberResponse("1234 5678 9012 3456");
        when(service.showNumber(cardId)).thenReturn(numberResponse);

        mockMvc.perform(get("/cards/{id}/number", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card_number").value("1234 5678 9012 3456"));
    }

}
