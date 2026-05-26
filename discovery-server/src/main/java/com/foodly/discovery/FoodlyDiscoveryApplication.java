package com.foodly.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FoodlyDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodlyDiscoveryApplication.class, args);
    }
}
