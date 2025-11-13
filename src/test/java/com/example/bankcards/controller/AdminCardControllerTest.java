package com.example.bankcards.controller;

import com.example.bankcards.AdminCardControllerConfig;
import com.example.bankcards.TestConfig;
import com.example.bankcards.controller.admin.AdminCardController;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.card.CardWithOwnerResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;
import com.example.bankcards.service.interfaces.card.CardAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, AdminCardControllerConfig.class})
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardAdminService service;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(service);
    }


    @Test
    void testGetAll_Success() throws Exception {
        ListCardResponse<CardWithOwnerResponse> response = new ListCardResponse<>();
        response.setItems(List.of(new CardWithOwnerResponse()));
        when(service.getAll(null, 1, 10)).thenReturn(response);

        mockMvc.perform(get("/admin/cards")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void testGetAll_EmptyList() throws Exception {
        ListCardResponse<CardWithOwnerResponse> response = new ListCardResponse<>();
        response.setItems(List.of());
        when(service.getAll(true, 1, 10)).thenReturn(response);

        mockMvc.perform(get("/admin/cards")
                        .param("haveBlockRequest", "true")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void testDelete_Success() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/admin/cards/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("Card not found")).when(service).delete(id);

        mockMvc.perform(delete("/admin/cards/{id}", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreate_Success() throws Exception {
        CreateCardRequest request = new CreateCardRequest(UUID.randomUUID());
        CardWithOwnerResponse response = new CardWithOwnerResponse();
        when(service.create(any(CreateCardRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testCreate_InvalidRequest() throws Exception {
        CreateCardRequest request = new CreateCardRequest(UUID.randomUUID());
        when(service.create(any(CreateCardRequest.class))).thenThrow(new RuntimeException("Some failed"));

        mockMvc.perform(post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testActivate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        CardWithOwnerResponse response = new CardWithOwnerResponse();
        when(service.activate(id)).thenReturn(response);

        mockMvc.perform(patch("/admin/cards/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testActivate_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.activate(id)).thenThrow(new RuntimeException("Card not found"));

        mockMvc.perform(patch("/admin/cards/{id}/activate", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testBlock_Success() throws Exception {
        UUID id = UUID.randomUUID();
        CardWithOwnerResponse response = new CardWithOwnerResponse();
        when(service.block(id)).thenReturn(response);

        mockMvc.perform(patch("/admin/cards/{id}/block", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testBlock_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.block(id)).thenThrow(new RuntimeException("Card not found"));

        mockMvc.perform(patch("/admin/cards/{id}/block", id))
                .andExpect(status().isInternalServerError());
    }
}
