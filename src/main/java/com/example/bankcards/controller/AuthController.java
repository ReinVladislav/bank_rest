package com.example.bankcards.controller;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.service.interfaces.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Регистрация, вход и работа с токенами")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final AuthenticationService service;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя и возвращает access/refresh токены"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserCreateRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @Operation(
            summary = "Вход в систему",
            description = "Возвращает пару токенов (access + refresh) при успешной аутентификации"
    )
    @ApiResponse(responseCode = "200", description = "Аутентификация успешна",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @Operation(
            summary = "Обновление access-токена",
            description = "Выдаёт новый access-токен по refresh-токену"
    )
    @ApiResponse(responseCode = "200", description = "Токен успешно обновлён",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(description = "Refresh-токен", required = true)
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        return ResponseEntity.ok(service.refresh(refreshToken));
    }

    @Operation(
            summary = "Выход из системы",
            description = "Инвалидирует refresh-токен (logout)"
    )
    @ApiResponse(responseCode = "200", description = "Пользователь успешно разлогинен")
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh-токен", required = true)
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        service.logout(refreshToken);
        return ResponseEntity.ok().build();
    }
}
