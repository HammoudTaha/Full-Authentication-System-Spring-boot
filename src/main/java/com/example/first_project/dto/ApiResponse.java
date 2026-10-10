package com.example.first_project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "message", "data", "error", "errorCode" })
public record ApiResponse<T> (
     boolean success,
     String message,
     T data,
     String errorCode
){
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null, null);
    }
    public static <T> ApiResponse<T> failure(String message,T data) {
        return new ApiResponse<>(false, message, data, null);
    }
    public static <T> ApiResponse<T> failure(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }
    public static <T> ApiResponse<T> failure(String message,T data , String errorCode) {
        return new ApiResponse<>(false, message, data, errorCode);
    }
}
