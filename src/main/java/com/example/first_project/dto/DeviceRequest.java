package com.example.first_project.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;
@lombok.Data
public class DeviceRequest {
    @NotNull(message = "Device ID is required")
    @Size(min = 64,max = 64, message = "Device ID is not valid")
    private String deviceId;
    @NotNull(message = "Device name is required")
    private String deviceName;
}
