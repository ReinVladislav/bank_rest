package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.response.AuthResponse;

public interface AuthenticationService {
    AuthResponse register(UserCreateRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String refreshToken);
    AuthResponse refresh(String refreshToken);
}
