package com.example.pizzashop.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EntityScan({
        "com.example.pizzashop.payment.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
@EnableJpaRepositories({
        "com.example.pizzashop.payment.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
