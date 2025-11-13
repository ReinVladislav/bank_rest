package com.example.bankcards.service.interfaces;

import com.example.bankcards.entity.UserModel;
import com.example.bankcards.util.enums.RoleType;

public interface UserCreator {
    UserModel create(String username, String encryptedPassword, RoleType roleType);
}
