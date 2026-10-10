package com.example.first_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginRequest extends DeviceRequest{
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^09\\d{8}$", message = "phone must be in format (e.g. 0987654321)")
    public String phone;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    public String password;
}
