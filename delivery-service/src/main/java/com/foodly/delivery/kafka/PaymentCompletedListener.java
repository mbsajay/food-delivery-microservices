package com.foodly.delivery.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes {@code payment.completed} and dispatches a courier. */
@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PaymentCompletedEvent.TOPIC, groupId = "delivery-service")
    public void onPaymentCompleted(String payload) throws Exception {
        PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
        deliveryService.dispatchForPayment(event);
    }
}
