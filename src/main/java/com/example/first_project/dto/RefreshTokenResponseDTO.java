package com.example.first_project.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"accessToken", "refreshToken"})
public record RefreshTokenResponseDTO(
        AuthResponseDTO accessToken,
        AuthResponseDTO refreshToken
) {
}
