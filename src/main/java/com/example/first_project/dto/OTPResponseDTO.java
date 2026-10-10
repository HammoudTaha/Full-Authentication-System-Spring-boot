package com.example.first_project.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.UUID;

@JsonPropertyOrder({"token", "tokenType", "expirationMinutes"})
public record OTPResponseDTO(UUID challengeId, int expiresInMinutes,int resendAfterInSeconds) {
}
