package com.example.bankcards.controller;

import com.example.bankcards.AuthControllerConfig;
import com.example.bankcards.TestConfig;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.service.interfaces.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, AuthControllerConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(service);
    }

    @Test
    void testRegister_Success() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        request.setRepeatPassword("qwe123");
        AuthResponse response = new AuthResponse();
        when(service.register(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        request.setRepeatPassword("qwe123");
        when(service.register(any(UserCreateRequest.class))).thenThrow(new RuntimeException("Validation failed"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        AuthResponse response = new AuthResponse();
        response.setAccessToken("authToken");
        response.setRefreshToken("refreshToken");
        when(service.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("authToken"))
                .andExpect(jsonPath("$.refresh_token").value("refreshToken"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        when(service.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testRefresh_Success() throws Exception {
        String refreshToken = "refresh-token";
        AuthResponse response = new AuthResponse();
        when(service.refresh(refreshToken)).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testRefresh_InvalidToken() throws Exception {
        String refreshToken = "invalid-token";
        when(service.refresh(refreshToken)).thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/auth/refresh")
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLogout_Success() throws Exception {
        String refreshToken = "refresh-token";
        doNothing().when(service).logout(refreshToken);

        mockMvc.perform(delete("/auth/logout")
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isOk());
    }

    @Test
    void testLogout_InvalidToken() throws Exception {
        String refreshToken = "invalid-token";
        doThrow(new RuntimeException("Invalid refresh token")).when(service).logout(refreshToken);

        mockMvc.perform(delete("/auth/logout")
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isInternalServerError());
    }
}
