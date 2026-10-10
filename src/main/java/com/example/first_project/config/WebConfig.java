package com.example.first_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
