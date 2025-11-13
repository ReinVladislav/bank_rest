package com.example.bankcards.service.interfaces;

import com.example.bankcards.entity.UserModel;
import com.example.bankcards.util.enums.RoleType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserCreator {
    UserModel getOne(UUID id);
    Page<UserModel> getAll(String username, RoleType type, int page, int size);
    UserModel update(UUID id, String username, String encodedPassword);
    void delete(UUID id);
}
