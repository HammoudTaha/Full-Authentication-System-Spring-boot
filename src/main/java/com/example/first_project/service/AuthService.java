package com.example.first_project.service;

import com.example.first_project.dto.*;
import com.example.first_project.entity.Device;
import com.example.first_project.entity.OTPCode;
import com.example.first_project.entity.PasswordResetToken;
import com.example.first_project.exception.BaseApiException;
import com.example.first_project.exception.InvalidOtpException;
import com.example.first_project.repository.DeviceRepository;
import com.example.first_project.repository.PasswordResetTokenRepository;
import com.example.first_project.utils.HashUtil;
import com.example.first_project.utils.OTPType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.first_project.entity.User;
import com.example.first_project.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final SessionService sessionService;
    private final DeviceRepository deviceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Clock clock;
    private static final SecureRandom random = new SecureRandom();

    public UserResponseDTO getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .map(this::convertToResponse)
                .orElse(null);
    }

    public OTPResponseDTO registerUser(RegisterRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("User with this phone number already exists.");
        }
        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
       return otpService.sendOTP(request.getPhone(), OTPType.VERIFICATION);
    }

    public OTPResponseDTO resendOTP(ResendOTPRequest request) {
        return otpService.resendOtp(request.getChallengeId());
    }

    public OTPResponseDTO forgetPassword(ForgetPasswordRequest request) {
        return otpService.sendOTP(request.getPhone(), OTPType.RESET_PASSWORD);
    }

    @Transactional(noRollbackFor = InvalidOtpException.class)
    public ResetTokenResponse verifyResetPassword(OTPRequest request) {
        OTPCode otpCode = otpService.verifyOtp(request, OTPType.RESET_PASSWORD);
        User user = userRepository.findByPhone(otpCode.getPhone()).orElseThrow(() -> new BaseApiException("Something went wrong."));
        String resetToken = generateResetToken();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setResetTokenHash(HashUtil.hash(resetToken));
        passwordResetToken.setExpiresAt(now().plusMinutes(5));
        passwordResetTokenRepository.save(passwordResetToken);
        return new ResetTokenResponse(resetToken,5);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request){
        LocalDateTime now = LocalDateTime.now();
        String hashToken = HashUtil.hash(request.resetToken());
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByResetTokenHash(hashToken)
                .orElseThrow(() -> new BaseApiException("Invalid reset token."));
        if (passwordResetToken.isExpired(now) || passwordResetToken.isUsed()) {
            throw new BaseApiException("Invalid reset token.");
        }
        passwordResetToken.markAsUsed(now);
        passwordResetTokenRepository.save(passwordResetToken);
        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }


    private static String generateResetToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    @Transactional(noRollbackFor = InvalidOtpException.class)
    public UserResponseDTO verifyPhone(VerifyPhoneRequest request,RequestMetadata rm) {
        OTPCode otpCode = otpService.verifyOtp(request.getOtpRequest(), OTPType.VERIFICATION);
        User user = userRepository.findByPhone(otpCode.getPhone()).orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setIsVerified(true);
        userRepository.save(user);
        UserResponseDTO response = convertToResponse(user);
        Device device = getDevice(request.getDeviceRequest().getDeviceId(),request.getDeviceRequest().getDeviceName(),user);
        RefreshTokenResponseDTO tokens = sessionService.createSession(device,rm);
                response.setAccessToken(tokens.accessToken());
                response.setRefreshToken(tokens.refreshToken());
        return response;
    }

    public UserResponseDTO loginUser(LoginRequest request,RequestMetadata rm) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("User with this phone number does not exist."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        if (!user.getIsVerified()) {
            OTPResponseDTO otpResponseDTO = otpService.sendOTP(user.getPhone(), OTPType.VERIFICATION);
            throw new BaseApiException("Your phone number is not verified. We have sent a verification code to your phone number Please verify your phone number.", HttpStatus.UNAUTHORIZED,"NOT_VERIFIED",otpResponseDTO);
        }
       Device device = getDevice(request.getDeviceId(),request.getDeviceName(),user);
        UserResponseDTO response = convertToResponse(user);
        RefreshTokenResponseDTO tokens =  sessionService.createSession(device,rm);
        response.setAccessToken(tokens.accessToken());
        response.setRefreshToken(tokens.refreshToken());
        return response;
    }

    private Device getDevice(String deviceId,String deviceName, User user) {
        return deviceRepository.findByDeviceIdAndUserId(deviceId, user.getId()).orElseGet(
                () -> {
                    Device newDevice = new Device();
                    newDevice.setDeviceId(deviceId);
                    newDevice.setUser(user);
                    newDevice.setDeviceName(deviceName);
                    newDevice.setLastUsedAt(LocalDateTime.now());
                    return deviceRepository.save(newDevice);
                });
    }


    @Transactional
    public void logoutUser(String accessToken) {
        sessionService.revokeDeviceSessions(accessToken);
    }

    @Transactional
    public void logoutAll(User user) {
        sessionService.revokeAllUserSessions(user);
    }

    public RefreshTokenResponseDTO refreshTokens(String refreshToken) {
        return sessionService.verifyAndRotate(refreshToken, new RequestMetadata("", ""));
    }

    private UserResponseDTO convertToResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

}
