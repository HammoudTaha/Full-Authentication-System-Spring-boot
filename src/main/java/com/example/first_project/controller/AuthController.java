package com.example.first_project.controller;

import com.example.first_project.Annotation.RateLimit;
import com.example.first_project.dto.*;
import com.example.first_project.entity.User;
import com.example.first_project.utils.ExtractAuthToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.first_project.service.AuthService;
import jakarta.validation.Valid;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService userService;
    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OTPResponseDTO>> registerUser(@Valid @RequestBody RegisterRequest request) {
        OTPResponseDTO otpResponseDTO = userService.registerUser(request);
        return new ResponseEntity<>(ApiResponse.success("User registered  successfully.", otpResponseDTO), HttpStatus.CREATED);
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<UserResponseDTO>> verifyOtp(@Valid VerifyPhoneRequest request,HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        RequestMetadata rm = new RequestMetadata(userAgent.length()>512?userAgent.substring(0,512):userAgent,httpRequest.getRemoteAddr());
        UserResponseDTO user = userService.verifyPhone(request,rm);
        return new ResponseEntity<>(ApiResponse.success("OTP verified successfully.", user), HttpStatus.OK);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<OTPResponseDTO>> resendOtp(@Valid ResendOTPRequest request) {
        OTPResponseDTO otpResponseDTO = userService.resendOTP(request);
        return new ResponseEntity<>(ApiResponse.success("OTP sent successfully.", otpResponseDTO), HttpStatus.OK);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<OTPResponseDTO>> forgotPassword(@Valid ForgetPasswordRequest request) {
        OTPResponseDTO response = userService.forgetPassword(request);
        return new ResponseEntity<>(ApiResponse.success("Password reset OTP sent successfully.", response), HttpStatus.OK);
    }

    @PostMapping("/verify-reset-password")
    public ResponseEntity<ApiResponse<ResetTokenResponse>> verifyResetPassword(@Valid OTPRequest request) {
        ResetTokenResponse response = userService.verifyResetPassword(request);
        return new ResponseEntity<>(ApiResponse.success("Password reset successfully.", response), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid ResetPasswordRequest request) {
        userService.resetPassword(request);
        return new ResponseEntity<>(ApiResponse.success("Password reset successfully.", null), HttpStatus.OK);
    }

    @PostMapping("/login")
    @RateLimit
    public ResponseEntity<ApiResponse<UserResponseDTO>> loginUser(@Valid LoginRequest request,HttpServletRequest httpRequest) {
        RequestMetadata rm = new RequestMetadata(httpRequest.getHeader("User-Agent"),httpRequest.getRemoteAddr());
        UserResponseDTO user = userService.loginUser(request,rm);
        return new ResponseEntity<>(ApiResponse.success("User logged in successfully.", user), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMe(
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(
                ApiResponse.success("User retrieved successfully.",
                        new UserResponseDTO(user)),
                HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(HttpServletRequest request) {
        Optional<String> token = ExtractAuthToken.extract(request);
        token.ifPresent(userService::logoutUser);
        return new ResponseEntity<>(ApiResponse.success("User logged out successfully.", null), HttpStatus.OK);
    }

    @PostMapping("/logout-device")
    public ResponseEntity<ApiResponse<Void>> logoutDevice(HttpServletRequest request) {
        Optional<String> token = ExtractAuthToken.extract(request);
        token.ifPresent(userService::logoutUser);
        return new ResponseEntity<>(ApiResponse.success("Device logged out successfully.", null), HttpStatus.OK);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal User user) {
        userService.logoutAll(user);
        return new ResponseEntity<>(ApiResponse.success("All user devices logged out successfully.", null), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshTokens(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponseDTO response = userService.refreshTokens( request.getRefreshToken());
        return new ResponseEntity<>(ApiResponse.success("Access token refreshed successfully.", response), HttpStatus.OK);
    }
}
