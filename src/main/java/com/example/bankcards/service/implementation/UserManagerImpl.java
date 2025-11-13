package com.example.bankcards.service.implementation;


import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.dto.response.ListUserResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.UserModel;
import com.example.bankcards.security.CustomUserDetail;
import com.example.bankcards.service.interfaces.UserManager;
import com.example.bankcards.service.interfaces.UserService;
import com.example.bankcards.util.constants.ErrorMessages;
import com.example.bankcards.util.enums.RoleType;
import com.example.bankcards.util.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagerImpl implements UserManager {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getOne(UUID id) {
        return userMapper.toResponse(userService.getOne(id));
    }

    @Override
    public ListUserResponse getAll(String username, int page, int size, RoleType type) {
        Page<UserModel> userPage = userService.getAll(username, type, page, size);
        return ListUserResponse.builder()
                .total(userPage.getTotalElements())
                .items(userPage.get()
                        .map(userMapper::toResponse)
                        .toList())
                .build();
    }

    @Override
    public void delete(UUID id) {
        UserModel currentUser = (
                (CustomUserDetail)SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        ).getUser();
        if (currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.DELETE_HIMSELF_ERROR);
        }
        userService.delete(id);
        log.info("User with id {} successfully deleted", id);
    }

    @Override
    public UserResponse create(UserCreateRequest request, RoleType type) {
        if (!request.getPassword().equals(request.getRepeatPassword())) {
            log.warn("Password mismatch during user creation for username='{}'", request.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        UserModel user = userService.create(
                request.getUsername(),
                encryptedPassword,
                type
        );
        log.info("User '{}' created successfully with role '{}'", user.getUsername(), type);
        return userMapper.toResponse(user);
    }


    @Override
    public UserResponse update(UUID id, UserUpdateRequest request) {
        String encryptedPassword = null;
        if (request.getPassword() != null) {
            if (!request.getPassword().equals(request.getRepeatPassword())) {
                log.warn("Password mismatch during user creation for username='{}'", request.getUsername());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.PASSWORDS_DO_NOT_MATCH);
            }
            encryptedPassword = passwordEncoder.encode(request.getPassword());
        }
        UserModel user = userService.update(id, request.getUsername(), encryptedPassword);
        log.info("User with id={} updated successfully", id);
        return userMapper.toResponse(user);
    }
}
