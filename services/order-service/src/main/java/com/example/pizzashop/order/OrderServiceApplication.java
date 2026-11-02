package com.example.pizzashop.order;

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
        "com.example.pizzashop.order.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
@EnableJpaRepositories({
        "com.example.pizzashop.order.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
