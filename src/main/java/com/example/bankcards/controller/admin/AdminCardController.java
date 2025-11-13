package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.CardWithOwnerResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;
import com.example.bankcards.service.interfaces.card.CardAdminService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/cards")
@Validated
@Tag(name = "Admin Cards", description = "Операции с картами для администраторов")
public class AdminCardController {

    private final CardAdminService service;

    @Operation(summary = "Получить все карты",
            description = "Возвращает список всех карт с возможностью фильтрации по запросу на блокировку")
    @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
            content = @Content(schema = @Schema(implementation = ListCardResponse.class)))
    @GetMapping
    public ResponseEntity<ListCardResponse<CardWithOwnerResponse>> getAll(
            @Parameter(description = "Фильтр по наличию запроса на блокировку (true/false)")
            @RequestParam(required = false) Boolean haveBlockRequest,
            @Parameter(description = "Номер страницы (min = 1)", required = true)
            @RequestParam @Min(1) int page,
            @Parameter(description = "Размер страницы (min = 1)", required = true)
            @RequestParam @Min(1) int size
    ){
        return ResponseEntity.ok(service.getAll(haveBlockRequest, page, size));
    }

    @Operation(summary = "Удалить карту",
            description = "Удаляет карту по UUID")
    @ApiResponse(responseCode = "204", description = "Карта успешно удалена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id
    ){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создать карту",
            description = "Создаёт новую карту и привязывает к владельцу")
    @ApiResponse(responseCode = "200", description = "Карта успешно создана",
            content = @Content(schema = @Schema(implementation = CardWithOwnerResponse.class)))
    @PostMapping
    public ResponseEntity<CardWithOwnerResponse> create(
            @RequestBody @Valid CreateCardRequest request
    ){
        return ResponseEntity.ok(service.create(request));
    }

    @Operation(summary = "Активировать карту",
            description = "Меняет статус карты на ACTIVE")
    @ApiResponse(responseCode = "200", description = "Карта успешно активирована",
            content = @Content(schema = @Schema(implementation = CardWithOwnerResponse.class)))
    @PatchMapping("/{id}/activate")
    public ResponseEntity<CardWithOwnerResponse> activate(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(service.activate(id));
    }

    @Operation(summary = "Заблокировать карту",
            description = "Меняет статус карты на BLOCKED")
    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
            content = @Content(schema = @Schema(implementation = CardWithOwnerResponse.class)))
    @PatchMapping("/{id}/block")
    public ResponseEntity<CardWithOwnerResponse> block(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(service.block(id));
    }
}
