package com.example.first_project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class ResendOTPRequest {
    @NotNull(message = "Challenge ID is required")
    private UUID challengeId;
}
