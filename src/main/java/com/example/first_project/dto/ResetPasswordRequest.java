package com.example.first_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required")
        String resetToken,
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 72,message = "Password must be at least 8 characters long")
        String newPassword
        ) {
}
