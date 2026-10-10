package com.example.first_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.core.AuthenticationException;
import com.example.first_project.dto.ApiResponse;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
                String clearMessage = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(java.util.stream.Collectors.joining(", "));

                ApiResponse<String> error = ApiResponse.failure(
                                "Validation Failed",
                                clearMessage,
                                "VALIDATION_ERROR");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex) {
                String clearMessage = ex.getMessage();
                ApiResponse<String> error = ApiResponse.failure(
                                "Invalid Request Body",
                                clearMessage,
                                "INVALID_REQUEST_BODY");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex) {
                String clearMessage = ex.getMessage();
                ApiResponse<String> error = ApiResponse.failure(
                                "Method Argument Type Mismatch",
                                clearMessage,
                                "METHOD_ARGUMENT_TYPE_MISMATCH");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<String>> handleBadRequest(IllegalArgumentException ex) {
                ApiResponse<String> error = ApiResponse.failure(
                                "Bad Request",
                                ex.getMessage(),
                                "BAD_REQUEST");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException ex) {
                ApiResponse<Void> error = ApiResponse.failure(
                                "Unauthorized to perform this action",
                                null,
                                "UNAUTHENTICATED");
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
                ApiResponse<Void> error = ApiResponse.failure(
                                "Authentication is required to access this endpoint",
                                null,
                                "UNAUTHENTICATED");
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(BaseApiException.class)
        public ResponseEntity<ApiResponse<Object>> handleBaseApiException(BaseApiException ex) {
                ApiResponse<Object> error = ApiResponse.failure(
                                ex.getMessage(),
                                ex.getData(),
                                ex.getErrorCode());
                return new ResponseEntity<>(error, ex.getStatus());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
                ApiResponse<String> error = ApiResponse.failure(
                                "Internal Server Error",
                                ex.getMessage(),
                                "INTERNAL_SERVER_ERROR");
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

}
