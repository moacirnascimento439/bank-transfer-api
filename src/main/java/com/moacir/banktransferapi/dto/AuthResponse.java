package com.moacir.banktransferapi.dto;

public record AuthResponse(
        String token,
        String username
) {
}
