package com.example.first_project.utils;

import com.example.first_project.exception.BaseApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public class ExtractAuthToken {
    private ExtractAuthToken() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static Optional<String> extract(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }
}
