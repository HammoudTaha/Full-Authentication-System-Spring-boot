package com.example.first_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmsPayload {
    public String to;
    public String message;
}
