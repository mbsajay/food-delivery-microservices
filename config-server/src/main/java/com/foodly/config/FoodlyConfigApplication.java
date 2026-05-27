package com.foodly.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class FoodlyConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodlyConfigApplication.class, args);
    }
}
