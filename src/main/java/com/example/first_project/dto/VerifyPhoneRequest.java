package com.example.first_project.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VerifyPhoneRequest {

    @Valid
    private final OTPRequest otpRequest;

    @Valid
    private final DeviceRequest deviceRequest;

    @JsonCreator
    public VerifyPhoneRequest(
            @JsonProperty("challengeId") UUID challengeId,
            @JsonProperty("otp") String otp,
            @JsonProperty("deviceId") String deviceId,
            @JsonProperty("deviceName") String deviceName) {

        this.otpRequest = new OTPRequest();
        this.otpRequest.setChallengeId(challengeId);
        this.otpRequest.setOtp(otp);

        this.deviceRequest = new DeviceRequest();
        this.deviceRequest.setDeviceId(deviceId);
        this.deviceRequest.setDeviceName(deviceName);
    }
}
