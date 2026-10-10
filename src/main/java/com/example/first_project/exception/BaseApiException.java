package com.example.first_project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final Object data;
    public BaseApiException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = null;
        this.data = null;
    }
    public BaseApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = null;
        this.data = null;
    }
    public BaseApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.data = null;
    }
    public BaseApiException(String message, HttpStatus status, Object data) {
        super(message);
        this.status = status;
        this.errorCode = null;
        this.data = data;
    }
    public BaseApiException(String message, HttpStatus status, String errorCode, Object data) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.data = data;
    }
}
