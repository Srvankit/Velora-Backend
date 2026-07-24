package com.veloramarkets.auth.dto;

public record AuthResponse(

        Long userId,
        String fullName,
        String username,
        String email,
        String role,
        String token,
        String tokenType,
        String message

) {
}