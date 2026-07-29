package com.veloramarkets.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30)
        String username,

        @Pattern(
                regexp = "^$|^[0-9+\\-() ]{8,20}$",
                message = "Invalid phone number"
        )
        String phone,

        @NotBlank(message = "Country is required")
        String country

) {}