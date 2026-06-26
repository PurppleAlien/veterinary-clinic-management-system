package com.veterinary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VeterinaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeterinaryApplication.class, args);
    }
}
