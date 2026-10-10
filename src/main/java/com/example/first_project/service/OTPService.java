package com.example.first_project.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.first_project.config.OtpProperties;
import com.example.first_project.dto.OTPRequest;
import com.example.first_project.dto.OTPResponseDTO;
import com.example.first_project.entity.OTPCode;
import com.example.first_project.exception.BaseApiException;
import com.example.first_project.exception.InvalidOtpException;
import com.example.first_project.repository.OTPRepository;
import com.example.first_project.utils.HashUtil;
import com.example.first_project.utils.OTPType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.example.first_project.dto.SmsPayload;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class OTPService {
    private final RabbitTemplate rabbitTemplate;
    private final OTPRepository otpRepository;
    private final OtpProperties otpProperties;
    private final Clock clock;
    private static final SecureRandom RANDOM = new SecureRandom();


    private OTPResponseDTO generateAndSend(String phone, OTPType type,LocalDateTime now) {
        String otpCode = String.format("%06d", RANDOM.nextInt(1_000_000));
        String hashOtpCode = HashUtil.hash(otpCode);
        OTPCode otpCodeEntity = new OTPCode();
        otpCodeEntity.setPhone(phone);
        otpCodeEntity.setCodeHash(hashOtpCode);
        otpCodeEntity.setCreatedAt(now);
        otpCodeEntity.setExpiresAt(now.plusMinutes(otpProperties.expirationMinutes()));
        otpCodeEntity.setType(type.name());
        otpRepository.save(otpCodeEntity);
        String smsMessage = "Your " + type.name().toLowerCase().replace('_', ' ') + " code is: " + otpCode;
        SmsPayload payload = new SmsPayload(phone, smsMessage);
        rabbitTemplate.convertAndSend("sms-queue", payload);
        return new OTPResponseDTO(otpCodeEntity.getId(), otpProperties.expirationMinutes(), 60);
    }

    @Transactional
    public OTPResponseDTO resendOtp(UUID id) {
        LocalDateTime now = now();
        OTPCode otpCode = otpRepository.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("No OTP code found."));
        if (otpCode.isConsumed() || otpCode.isInvalidate()) {
            throw new BaseApiException("OTP code has already been consumed or invalidated. Please request a new OTP.");
        }
        return reissue(otpCode, now);
    }

    public OTPResponseDTO sendOTP(String phone, OTPType type){
        LocalDateTime now = now();
        return otpRepository.findActive(phone, type.name(),now, PageRequest.of(0, 1))
                .stream().findFirst().map(otp -> reissue(otp, now) ).orElseGet(() -> generateAndSend(phone, type,now));
    }
    private OTPResponseDTO reissue(OTPCode old,LocalDateTime now){
        if (old.getCreatedAt().isAfter(now.minusSeconds(otpProperties.resendSeconds()))) {
            throw new BaseApiException("You can only resend the OTP after 1 minute.");
        }
        old.invalidate(now);
        otpRepository.save(old);
        return generateAndSend(old.getPhone(), old.getType(),now);
    }


    public OTPCode verifyOtp(OTPRequest request, OTPType type) {
        LocalDateTime now = now();
        OTPCode otpCode = otpRepository.findByIdForUpdate(request.getChallengeId())
                .orElseThrow(() -> new BaseApiException("OTP code not found."));
        if (otpCode.getType().equals(type)) {
            throw new BaseApiException("Invalid OTP type.");
        }
        validateOtpStatus(otpCode, now);
        validateOtpHash(otpCode, request.getOtp());
        otpCode.consume(now);
        otpRepository.save(otpCode);
        return otpCode;
    }

    private void validateOtpStatus(OTPCode otpCode,LocalDateTime now) {
        if (otpCode.isConsumed()) {
            throw new BaseApiException("OTP code has already been consumed.");
        }
        if (otpCode.isExpired(now)) {
            throw new BaseApiException("OTP code has expired.");
        }
        if (!otpCode.canAttempt(otpProperties.maxAttempts())) {
            throw new BaseApiException("OTP code has reached the maximum number of attempts.");
        }
    }

    private void validateOtpHash(OTPCode otpCode, String otp) {
        if (!HashUtil.hash(otp).equals(otpCode.getCodeHash())) {
            otpCode.incrementAttempts();
            otpRepository.save(otpCode);
            throw new InvalidOtpException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

}