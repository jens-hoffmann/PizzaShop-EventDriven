package com.example.pizzashop.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan({
        "com.example.pizzashop.notification.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
@EnableJpaRepositories({
        "com.example.pizzashop.notification.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
