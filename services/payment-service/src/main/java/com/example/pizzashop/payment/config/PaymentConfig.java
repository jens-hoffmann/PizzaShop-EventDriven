package com.example.pizzashop.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PaymentConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
