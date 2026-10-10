package com.example.first_project.dto;

import java.time.LocalDateTime;

public record ResetTokenResponse(String resetToken, int expiresInMinutes) {
}
