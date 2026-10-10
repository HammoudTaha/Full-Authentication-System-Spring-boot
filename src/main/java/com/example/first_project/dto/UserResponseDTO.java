package com.example.first_project.dto;

import com.example.first_project.entity.User;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@lombok.Data
@JsonPropertyOrder({ "id", "name", "phone", "createdAt", "updatedAt", "authResponseDTO" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private String name;
    private String phone;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm a")
    private LocalDateTime updatedAt;
    private AuthResponseDTO accessToken;
    private AuthResponseDTO refreshToken;

    public UserResponseDTO() {
    }

    public UserResponseDTO(User user) {
        this.name = user.getName();
        this.phone = user.getPhone();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
