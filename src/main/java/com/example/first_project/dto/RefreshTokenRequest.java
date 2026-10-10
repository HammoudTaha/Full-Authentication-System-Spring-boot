package com.example.first_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@lombok.Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    @Size(min = 36, max = 36, message = "Invalid format of refresh token")
    private String refreshToken;
}
