package com.foodly.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodlyPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodlyPaymentApplication.class, args);
    }
}
