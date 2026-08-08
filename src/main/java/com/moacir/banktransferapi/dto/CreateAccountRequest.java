package com.moacir.banktransferapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountRequest(

        @NotBlank(message = "O número da conta é obrigatório")
        String accountNumber,

        @NotBlank(message = "O nome do titular é obrigatório")
        String ownerName,

        @PositiveOrZero(message = "O saldo inicial não pode ser negativo")
        BigDecimal initialBalance
) {
}