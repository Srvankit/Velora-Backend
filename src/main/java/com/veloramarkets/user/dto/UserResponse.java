package com.veloramarkets.user.dto;

public record UserResponse(

        Long id,
        String fullName,
        String username,
        String email,
        String phone,
        String country,
        String role,
        boolean emailVerified

) {
}