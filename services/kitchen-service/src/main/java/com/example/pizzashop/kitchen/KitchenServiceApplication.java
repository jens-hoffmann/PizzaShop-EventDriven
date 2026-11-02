package com.example.pizzashop.kitchen;

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
        "com.example.pizzashop.kitchen.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
@EnableJpaRepositories({
        "com.example.pizzashop.kitchen.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
public class KitchenServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KitchenServiceApplication.class, args);
    }
}
