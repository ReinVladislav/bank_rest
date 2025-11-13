package com.example.bankcards.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.security.CustomUserDetail;
import com.example.bankcards.service.implementation.UserManagerImpl;
import com.example.bankcards.service.interfaces.UserService;
import com.example.bankcards.util.enums.RoleType;
import com.example.bankcards.util.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class UserManagerImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagerImpl userManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOne_ReturnsMappedUser() {
        UUID id = UUID.randomUUID();
        UserModel userModel = UserModel.builder()
                .id(id)
                .username("test")
                .build();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(id);
        userResponse.setUsername("test");

        when(userService.getOne(id)).thenReturn(userModel);
        when(userMapper.toResponse(userModel)).thenReturn(userResponse);

        UserResponse result = userManager.getOne(id);

        assertEquals("test", result.getUsername());
        verify(userService).getOne(id);
        verify(userMapper).toResponse(userModel);
    }

    @Test
    void testGetAll_ReturnsListUserResponse() {
        UserModel userModel = UserModel.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .build();
        Page<UserModel> page = new PageImpl<>(List.of(userModel));
        UserResponse response = new UserResponse();
        response.setId(userModel.getId());
        response.setUsername("user1");

        when(userService.getAll("user", null, 0, 10)).thenReturn(page);
        when(userMapper.toResponse(userModel)).thenReturn(response);

        var result = userManager.getAll("user", 0, 10, null);

        assertEquals(1, result.getTotal());
        assertEquals("user1", result.getItems().get(0).getUsername());
    }

    @Test
    void testDelete_OtherUser_DeletesSuccessfully() {
        UUID idToDelete = UUID.randomUUID();
        UserModel currentUser = UserModel.builder()
                .id(UUID.randomUUID())
                .username("me")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new CustomUserDetail(currentUser));

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        userManager.delete(idToDelete);

        verify(userService).delete(idToDelete);
    }

    @Test
    void testDelete_SelfUser_ThrowsException() {
        UUID idToDelete = UUID.randomUUID();
        UserModel currentUser = UserModel.builder()
                .id(idToDelete)
                .username("me")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new CustomUserDetail(currentUser));

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userManager.delete(idToDelete));
        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testCreate_Success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newUser");
        request.setPassword("pass");
        request.setRepeatPassword("pass");

        UserModel createdUser = UserModel.builder()
                .id(UUID.randomUUID())
                .username("newUser")
                .build();
        UserResponse response = new UserResponse();
        response.setId(createdUser.getId());
        response.setUsername("newUser");

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userService.create("newUser", "encodedPass", RoleType.USER)).thenReturn(createdUser);
        when(userMapper.toResponse(createdUser)).thenReturn(response);

        UserResponse result = userManager.create(request, RoleType.USER);

        assertEquals("newUser", result.getUsername());
        verify(userService).create("newUser", "encodedPass", RoleType.USER);
    }

    @Test
    void testCreate_PasswordMismatch_ThrowsException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newUser");
        request.setPassword("pass1");
        request.setRepeatPassword("pass2");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userManager.create(request, RoleType.USER));
        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    // ===== update =====
    @Test
    void testUpdate_Success_WithPassword() {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updated");
        request.setPassword("pass");
        request.setRepeatPassword("pass");

        UserModel updatedUser = UserModel.builder().id(id).username("updated").build();
        UserResponse response = new UserResponse();
        response.setId(id);
        response.setUsername("updated");

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userService.update(id, "updated", "encodedPass")).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(response);

        UserResponse result = userManager.update(id, request);

        assertEquals("updated", result.getUsername());
    }

    @Test
    void testUpdate_Success_WithoutPassword() {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updated");

        UserModel updatedUser = UserModel.builder().id(id).username("updated").build();
        UserResponse response = new UserResponse();
        response.setId(id);
        response.setUsername("updated");

        when(userService.update(id, "updated", null)).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(response);

        UserResponse result = userManager.update(id, request);

        assertEquals("updated", result.getUsername());
    }

    @Test
    void testUpdate_PasswordMismatch_ThrowsException() {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updated");
        request.setPassword("p1");
        request.setRepeatPassword("p2");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userManager.update(id, request));
        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }
}

