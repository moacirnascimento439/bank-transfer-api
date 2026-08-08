package com.moacir.banktransferapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "A conta de origem é obrigatória")
        String fromAccountNumber,

        @NotBlank(message = "A conta de destino é obrigatória")
        String toAccountNumber,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal amount
) {
}