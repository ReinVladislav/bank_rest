package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenUtils;
import com.example.bankcards.service.implementation.AuthenticationServiceImpl;
import com.example.bankcards.service.interfaces.UserCreator;
import com.example.bankcards.util.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceImplTest {

    @Mock
    private UserCreator userCreator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtils jwtTokenUtils;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserModel user;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        refreshToken = "refresh123";

        user = UserModel.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("encodedPass")
                .isDeleted(false)
                .build();
    }


    @Test
    void testRegister_Success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newUser");
        request.setPassword("pass");
        request.setRepeatPassword("pass");

        UserModel createdUser = UserModel.builder()
                .id(UUID.randomUUID())
                .username("newUser")
                .build();

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userCreator.create("newUser", "encodedPass", RoleType.USER)).thenReturn(createdUser);
        when(jwtTokenUtils.generateRefreshToken(createdUser)).thenReturn("newRefresh");
        when(jwtTokenUtils.generateAccessToken(createdUser)).thenReturn("access123");

        AuthResponse result = authenticationService.register(request);

        assertEquals("access123", result.getAccessToken());
        assertEquals("newRefresh", result.getRefreshToken());
        verify(userRepository).save(createdUser);
    }

    @Test
    void testRegister_PasswordMismatch_ThrowsException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newUser");
        request.setPassword("a");
        request.setRepeatPassword("b");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("pass");

        when(userRepository.findByUsernameAndIsDeleted("testuser", false)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        when(jwtTokenUtils.generateRefreshToken(user)).thenReturn("newRefresh");
        when(jwtTokenUtils.generateAccessToken(user)).thenReturn("access123");

        AuthResponse result = authenticationService.login(request);

        assertEquals("access123", result.getAccessToken());
        assertEquals("newRefresh", result.getRefreshToken());
        verify(userRepository).save(user);
    }

    @Test
    void testLogin_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        when(userRepository.findByUsernameAndIsDeleted("testuser", false)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghost");
        request.setPassword("123");

        when(userRepository.findByUsernameAndIsDeleted("ghost", false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testLogout_Success() {
        when(userRepository.findByRefreshTokenAndIsDeleted(refreshToken, false))
                .thenReturn(Optional.of(user));

        authenticationService.logout(refreshToken);

        assertNull(user.getRefreshToken());
        verify(userRepository).save(user);
    }

    @Test
    void testLogout_InvalidToken_ThrowsException() {
        when(userRepository.findByRefreshTokenAndIsDeleted(refreshToken, false))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.logout(refreshToken));

        assertEquals(HttpStatus.UNAUTHORIZED, HttpStatus.valueOf(ex.getStatusCode().value()));
    }


    @Test
    void testRefresh_Success() {
        when(userRepository.findByRefreshTokenAndIsDeleted(refreshToken, false))
                .thenReturn(Optional.of(user));
        when(jwtTokenUtils.generateRefreshToken(user)).thenReturn("newRefresh");
        when(jwtTokenUtils.generateAccessToken(user)).thenReturn("newAccess");

        AuthResponse result = authenticationService.refresh(refreshToken);

        assertEquals("newAccess", result.getAccessToken());
        assertEquals("newRefresh", result.getRefreshToken());
        verify(userRepository).save(user);
    }

    @Test
    void testRefresh_InvalidToken_ThrowsException() {
        when(userRepository.findByRefreshTokenAndIsDeleted(refreshToken, false))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.refresh(refreshToken));

        assertEquals(HttpStatus.UNAUTHORIZED, HttpStatus.valueOf(ex.getStatusCode().value()));
    }
}

