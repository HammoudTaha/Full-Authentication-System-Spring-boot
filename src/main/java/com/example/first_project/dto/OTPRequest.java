package com.example.first_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@lombok.Data
public class OTPRequest {
    @NotNull(message = "Challenge ID is required")
    private UUID challengeId;
    @NotBlank(message = "OTP code is required")
    @Size(min = 6,max = 6, message = "OTP must be at least 6 characters long")
    private String otp;
}
