package com.example.pizzashop.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan({
        "com.example.pizzashop.catalog.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
@EnableJpaRepositories({
        "com.example.pizzashop.catalog.adapter.out.persistence",
        "com.example.pizzashop.messaging"
})
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
