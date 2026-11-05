package com.ellora.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot composition root for the Ellora Booking Monolith.
 * Scans all packages under 'com.ellora' to register components across layers.
 */
@SpringBootApplication(scanBasePackages = "com.ellora")
public class ElloraApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElloraApplication.class, args);
    }
}
