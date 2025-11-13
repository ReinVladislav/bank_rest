package com.example.bankcards.controller;

import com.example.bankcards.dto.request.DepositRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.card.CardBalanceResponse;
import com.example.bankcards.dto.response.card.CardNumberResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.dto.response.card.ListCardResponse;
import com.example.bankcards.service.interfaces.card.CardOwnerService;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cards")
@Validated
@Tag(name = "User Cards", description = "Операции с картами пользователя")
public class UserCardController {

    private final CardOwnerService service;

    @Operation(summary = "Получить список своих карт",
            description = "Возвращает список карт текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список карт успешно получен",
            content = @Content(schema = @Schema(implementation = ListCardResponse.class)))
    @GetMapping
    public ResponseEntity<ListCardResponse<CardResponse>> getMyCards(
            @Parameter(description = "Номер страницы (min = 1)", required = true)
            @RequestParam @NotNull @Min(1) int page,
            @Parameter(description = "Размер страницы (min = 1)", required = true)
            @RequestParam @NotNull @Min(1) int size
    ) {
        return ResponseEntity.ok(service.getAllMyCards(page, size));
    }

    @Operation(summary = "Перевод средств между картами пользователя",
            description = "Позволяет перевести средства с одной карты на другую")
    @ApiResponse(responseCode = "204", description = "Перевод выполнен успешно")
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestBody @Valid TransferRequest request
    ) {
        service.transfer(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пополнение баланса карты",
            description = "Позволяет пополнить баланс карты")
    @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен",
            content = @Content(schema = @Schema(implementation = CardBalanceResponse.class)))
    @PatchMapping("/{id}/deposit")
    public ResponseEntity<CardBalanceResponse> deposit(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid DepositRequest request
    ) {
        return ResponseEntity.ok(service.deposit(id, request));
    }

    @Operation(summary = "Запрос на блокировку карты",
            description = "Отправляет запрос на блокировку карты пользователя")
    @ApiResponse(responseCode = "200", description = "Карта успешно помечена как 'Запрошена блокировка'",
            content = @Content(schema = @Schema(implementation = CardResponse.class)))
    @PatchMapping("/{id}/block")
    public ResponseEntity<CardResponse> blockCardRequest(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.blockRequest(id));
    }

    @Operation(summary = "Проверка баланса карты",
            description = "Возвращает текущий баланс указанной карты")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
            content = @Content(schema = @Schema(implementation = CardBalanceResponse.class)))
    @GetMapping("/{id}/balance")
    public ResponseEntity<CardBalanceResponse> checkBalance(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.showBalance(id));
    }

    @Operation(summary = "Показать номер карты",
            description = "Возвращает номер карты без маски")
    @ApiResponse(responseCode = "200", description = "Номер карты успешно получен",
            content = @Content(schema = @Schema(implementation = CardNumberResponse.class)))
    @GetMapping("/{id}/number")
    public ResponseEntity<CardNumberResponse> showCardNumber(
            @Parameter(description = "UUID карты", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.showNumber(id));
    }
}
