package com.example.first_project.Listener;

import com.example.first_project.dto.SmsPayload;
import com.example.first_project.service.SMSService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SMSListener {
    private final SMSService smsService;

    public SMSListener(SMSService smsService) {
        this.smsService = smsService;
    }

    @RabbitListener(queues = "sms-queue")
    public void onMessage(SmsPayload payload) {
        smsService.sendMessage(payload.getTo(), payload.getMessage());
    }
}
