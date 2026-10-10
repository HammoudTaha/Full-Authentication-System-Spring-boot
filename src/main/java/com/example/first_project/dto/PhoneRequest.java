package com.example.first_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneRequest {
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^09\\d{8}$", message = "phone must be in format (e.g. 0987654321)")
    public String phone;
}
