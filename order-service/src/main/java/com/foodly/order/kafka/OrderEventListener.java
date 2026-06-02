package com.foodly.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodly.common.event.OrderDeliveredEvent;
import com.foodly.common.event.OrderDispatchedEvent;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.common.event.PaymentFailedEvent;
import com.foodly.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Drives the order state machine from downstream events:
 * payment.completed → CONFIRMED, payment.failed → CANCELLED,
 * order.dispatched → OUT_FOR_DELIVERY, order.delivered → DELIVERED.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PaymentCompletedEvent.TOPIC, groupId = "order-service")
    public void onPaymentCompleted(String payload) throws Exception {
        PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
        orderService.markConfirmed(UUID.fromString(event.getOrderId()), UUID.fromString(event.getPaymentId()));
    }

    @KafkaListener(topics = PaymentFailedEvent.TOPIC, groupId = "order-service")
    public void onPaymentFailed(String payload) throws Exception {
        PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
        orderService.markCancelled(UUID.fromString(event.getOrderId()));
    }

    @KafkaListener(topics = OrderDispatchedEvent.TOPIC, groupId = "order-service")
    public void onOrderDispatched(String payload) throws Exception {
        OrderDispatchedEvent event = objectMapper.readValue(payload, OrderDispatchedEvent.class);
        orderService.markOutForDelivery(UUID.fromString(event.getOrderId()), event.getCourierId());
    }

    @KafkaListener(topics = OrderDeliveredEvent.TOPIC, groupId = "order-service")
    public void onOrderDelivered(String payload) throws Exception {
        OrderDeliveredEvent event = objectMapper.readValue(payload, OrderDeliveredEvent.class);
        orderService.markDelivered(UUID.fromString(event.getOrderId()));
    }
}
