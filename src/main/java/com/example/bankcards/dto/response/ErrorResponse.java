package com.example.bankcards.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    @NotNull
    private Integer status;
    private String title;
    private String detail;
    private LocalDateTime timestamp;
}
