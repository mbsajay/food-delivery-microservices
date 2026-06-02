package com.foodly.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodly.common.event.OrderPlacedEvent;
import com.foodly.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes {@code order.placed} and kicks off payment processing. */
@Component
@RequiredArgsConstructor
public class OrderPlacedListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OrderPlacedEvent.TOPIC, groupId = "payment-service")
    public void onOrderPlaced(String payload) throws Exception {
        OrderPlacedEvent event = objectMapper.readValue(payload, OrderPlacedEvent.class);
        paymentService.processOrderPlaced(event);
    }
}
