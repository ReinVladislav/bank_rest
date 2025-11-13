package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.dto.response.ListUserResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.util.enums.RoleType;

import java.util.List;
import java.util.UUID;

public interface UserManager {
    UserResponse getOne(UUID id);
    UserResponse update(UUID id, UserUpdateRequest request);
    void delete(UUID id);
    ListUserResponse getAll(String username, int page, int size, RoleType type);
    UserResponse create(UserCreateRequest request, RoleType type);

}
