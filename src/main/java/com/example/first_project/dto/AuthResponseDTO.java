package com.example.first_project.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@JsonPropertyOrder({ "token", "tokenType", "expirationMinutes" })
public class AuthResponseDTO {
    private String token;
    private String tokenType = "Bearer";
    private int expirationMinutes;
}
