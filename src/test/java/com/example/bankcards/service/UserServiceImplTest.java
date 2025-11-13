package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.implementation.UserServiceImpl;
import com.example.bankcards.util.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID id;
    private UserModel user;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        role = new Role();
        role.setName("ROLE_USER");

        user = UserModel.builder()
                .id(id)
                .username("testuser")
                .password("pass")
                .isDeleted(false)
                .role(role)
                .build();
    }

    @Test
    void testGetOne_Found() {
        when(userRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(user));

        UserModel result = userService.getOne(id);

        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByIdAndIsDeleted(id, false);
    }

    @Test
    void testGetOne_NotFound_ThrowsException() {
        when(userRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.getOne(id));

        assertEquals(HttpStatus.NOT_FOUND,  HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testGetAll_WithUsername() {
        Page<UserModel> page = new PageImpl<>(List.of(user));
        when(userRepository.findByRole_NameAndUsernameContainsAndIsDeleted(eq("ROLE_USER"), eq("test"), eq(false), any()))
                .thenReturn(page);

        Page<UserModel> result = userService.getAll("test", RoleType.USER, 1, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAll_WithoutUsername() {
        Page<UserModel> page = new PageImpl<>(List.of(user));
        when(userRepository.findByRole_NameAndIsDeleted(eq("ROLE_USER"), eq(false), any(Pageable.class)))
                .thenReturn(page);

        Page<UserModel> result = userService.getAll(null, RoleType.USER, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdate_Success() {
        when(userRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIsDeleted("newName", false)).thenReturn(false);
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));

        UserModel result = userService.update(id, "newName", "newPass");

        assertEquals("newName", result.getUsername());
        assertEquals("newPass", result.getPassword());
        assertNull(result.getRefreshToken());
    }

    @Test
    void testUpdate_UsernameExists_ThrowsException() {
        when(userRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIsDeleted("otherUser", false)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.update(id, "otherUser", null));

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testDelete_Success() {
        when(userRepository.findByIdAndIsDeleted(id, false)).thenReturn(Optional.of(user));

        userService.delete(id);

        assertTrue(user.getIsDeleted());
        verify(userRepository).save(user);
    }

    @Test
    void testCreate_Success() {
        when(userRepository.existsByUsernameAndIsDeleted("newUser", false)).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));

        UserModel result = userService.create("newUser", "encodedPass", RoleType.USER);

        assertEquals("newUser", result.getUsername());
        assertEquals("encodedPass", result.getPassword());
        assertEquals(role, result.getRole());
    }

    @Test
    void testCreate_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsernameAndIsDeleted("newUser", false)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.create("newUser", "pass", RoleType.USER));

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @Test
    void testCreate_RoleNotFound_ThrowsException() {
        when(userRepository.existsByUsernameAndIsDeleted("newUser", false)).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.create("newUser", "pass", RoleType.USER));
    }
}
