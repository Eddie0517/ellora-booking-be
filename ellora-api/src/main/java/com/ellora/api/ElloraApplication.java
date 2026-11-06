package com.ellora.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot composition root for the Ellora Booking Monolith.
 * Scans all packages under 'com.ellora' to register components across layers.
 */
@SpringBootApplication(scanBasePackages = "com.ellora")
@EntityScan(basePackages = "com.ellora.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.ellora.infrastructure.persistence.repository")
public class ElloraApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElloraApplication.class, args);
    }
}
