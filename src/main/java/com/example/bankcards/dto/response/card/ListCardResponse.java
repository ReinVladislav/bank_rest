package com.example.bankcards.dto.response.card;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ListCardResponse<T extends CardResponse> {
    @NotNull
    private List<T> items;
    @NotNull
    private Long total;
    public static <T extends CardResponse> ListCardResponse<T> of(long total, List<T> items) {
        ListCardResponse<T> response = new ListCardResponse<>();
        response.setItems(items);
        response.setTotal(total);
        return response;
    }
}
