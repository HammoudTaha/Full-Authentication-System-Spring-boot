package com.example.first_project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        int expirationMinutes,
        int maxAttempts,
        int resendSeconds
) {}
