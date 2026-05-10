package com.smakroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmakRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmakRoomApplication.class, args);
    }
}
