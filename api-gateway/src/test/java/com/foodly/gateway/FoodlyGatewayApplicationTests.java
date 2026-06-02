package com.foodly.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class FoodlyGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void healthEndpointIsUp() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
