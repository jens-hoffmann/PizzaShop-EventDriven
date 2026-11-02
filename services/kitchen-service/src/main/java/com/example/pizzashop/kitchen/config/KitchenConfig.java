package com.example.pizzashop.kitchen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class KitchenConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
