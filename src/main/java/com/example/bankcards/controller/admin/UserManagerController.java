package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.request.UserCreateRequest;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.dto.response.ListUserResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.interfaces.UserManager;
import com.example.bankcards.util.enums.RoleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Management (Admin)", description = "Управление пользователями и администраторами")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@Validated
public class UserManagerController {

    private final UserManager userManager;

    @Operation(summary = "Получить информацию о пользователе по ID",
            description = "Возвращает данные пользователя по UUID")
    @ApiResponse(responseCode = "200", description = "Успешный ответ",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getOne(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userManager.getOne(id));
    }

    @Operation(summary = "Обновить данные пользователя",
            description = "Частично обновляет данные пользователя по UUID")
    @ApiResponse(responseCode = "200", description = "Данные успешно обновлены")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userManager.update(id, request));
    }

    @Operation(summary = "Удалить пользователя",
            description = "Логическое или физическое удаление пользователя по UUID")
    @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id
    ) {
        userManager.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить список обычных пользователей",
            description = "Возвращает список пользователей с ролью USER с пагинацией и фильтрацией по имени")
    @ApiResponse(responseCode = "200", description = "Список пользователей",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping
    public ResponseEntity<ListUserResponse> getAllUser(
            @Parameter(description = "Фильтр по имени пользователя") @RequestParam(required = false) String username,
            @Parameter(description = "Номер страницы (min = 1)") @RequestParam @NotNull @Min(1) int page,
            @Parameter(description = "Размер страницы (min = 1)") @RequestParam @NotNull @Min(1) int size
    ) {
        return ResponseEntity.ok(
                userManager.getAll(username, page, size, RoleType.USER)
        );
    }

    @Operation(summary = "Получить список администраторов",
            description = "Возвращает список пользователей с ролью ADMIN с пагинацией и фильтрацией по имени")
    @ApiResponse(responseCode = "200", description = "Список администраторов",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/admins")
    public ResponseEntity<ListUserResponse> getAllAdmin(
            @Parameter(description = "Фильтр по имени администратора") @RequestParam(required = false) String username,
            @Parameter(description = "Номер страницы (min = 1)") @RequestParam @NotNull @Min(1) int page,
            @Parameter(description = "Размер страницы (min = 1)") @RequestParam @NotNull @Min(1) int size
    ) {
        return ResponseEntity.ok(
                userManager.getAll(username, page, size, RoleType.ADMIN)
        );
    }

    @Operation(summary = "Создать нового пользователя",
            description = "Создаёт пользователя с ролью USER")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManager.create(request, RoleType.USER));
    }

    @Operation(summary = "Создать администратора",
            description = "Создаёт нового пользователя с ролью ADMIN")
    @ApiResponse(responseCode = "201", description = "Администратор успешно создан")
    @PostMapping("/admin")
    public ResponseEntity<UserResponse> createAdmin(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManager.create(request, RoleType.ADMIN));
    }
}
