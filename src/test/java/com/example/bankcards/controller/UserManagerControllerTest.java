package com.example.bankcards.controller;

import com.example.bankcards.TestConfig;
import com.example.bankcards.UserManagerControllerConfig;
import com.example.bankcards.controller.admin.UserManagerController;
import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.dto.response.ListUserResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.interfaces.UserManager;
import com.example.bankcards.util.enums.RoleType;
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

@WebMvcTest(UserManagerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestConfig.class, UserManagerControllerConfig.class})
class UserManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserManager userManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(userManager);
    }

    @Test
    void testGetOne_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse();
        when(userManager.getOne(id)).thenReturn(response);

        mockMvc.perform(get("/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetOne_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userManager.getOne(id)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/admin/users/{id}", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("testUser");
        UserResponse response = new UserResponse();
        when(userManager.update(eq(id), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/admin/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("testUser");
        when(userManager.update(eq(id), any(UserUpdateRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(patch("/admin/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDelete_Success() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(userManager).delete(id);

        mockMvc.perform(delete("/admin/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("User not found")).when(userManager).delete(id);

        mockMvc.perform(delete("/admin/users/{id}", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllUser_Success() throws Exception {
        ListUserResponse response = new ListUserResponse();
        response.setItems(List.of(new UserResponse()));
        when(userManager.getAll(null, 1, 10, RoleType.USER)).thenReturn(response);

        mockMvc.perform(get("/admin/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void testGetAllUser_EmptyList() throws Exception {
        ListUserResponse response = new ListUserResponse();
        response.setItems(List.of());
        when(userManager.getAll("John", 1, 10, RoleType.USER)).thenReturn(response);

        mockMvc.perform(get("/admin/users")
                        .param("username", "John")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void testGetAllAdmin_Success() throws Exception {
        ListUserResponse response = new ListUserResponse();
        response.setItems(List.of(new UserResponse()));
        when(userManager.getAll(null, 1, 10, RoleType.ADMIN)).thenReturn(response);

        mockMvc.perform(get("/admin/users/admins")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void testGetAllAdmin_EmptyList() throws Exception {
        ListUserResponse response = new ListUserResponse();
        response.setItems(List.of());
        when(userManager.getAll("Admin", 1, 10, RoleType.ADMIN)).thenReturn(response);

        mockMvc.perform(get("/admin/users/admins")
                        .param("username", "Admin")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        UserResponse response = new UserResponse();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        request.setRepeatPassword("qwe123");
        response.setId(UUID.randomUUID());
        response.setUsername("testUser");
        when(userManager.create(request, RoleType.USER)).thenReturn(response);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testCreateUser_InvalidRequest() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        request.setRepeatPassword("qwe123");
        when(userManager.create(request, RoleType.USER)).thenThrow(new RuntimeException("Validation failed"));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateAdmin_Success() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        UserResponse response = new UserResponse();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        request.setRepeatPassword("qwe123");
        response.setId(UUID.randomUUID());
        response.setUsername("testUser");
        when(userManager.create(request, RoleType.ADMIN)).thenReturn(response);

        mockMvc.perform(post("/admin/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testCreateAdmin_InvalidRequest() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testUser");
        request.setPassword("qwe123");
        request.setRepeatPassword("qwe123");
        when(userManager.create(request, RoleType.ADMIN)).thenThrow(new RuntimeException("Validation failed"));

        mockMvc.perform(post("/admin/users/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
