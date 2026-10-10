package com.example.first_project.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BaseApiException {
    public InvalidOtpException() {
        super("Invalid OTP code.", HttpStatus.BAD_REQUEST,"INVALID_OTP");
    }
}
