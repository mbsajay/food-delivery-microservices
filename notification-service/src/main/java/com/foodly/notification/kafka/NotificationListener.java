package com.foodly.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodly.common.event.OrderDeliveredEvent;
import com.foodly.common.event.OrderDispatchedEvent;
import com.foodly.common.event.OrderPlacedEvent;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.common.event.PaymentFailedEvent;
import com.foodly.notification.model.Notification;
import com.foodly.notification.store.NotificationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Fans every lifecycle event out to a notification. In a real system these would be
 * email/SMS/push; here they are recorded to an in-memory buffer and the log.
 */
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationStore store;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OrderPlacedEvent.TOPIC, groupId = "notification-service")
    public void onOrderPlaced(String payload) throws Exception {
        OrderPlacedEvent e = objectMapper.readValue(payload, OrderPlacedEvent.class);
        store.record(Notification.of(e.getType(), e.getAggregateId(),
                "Your order has been placed and is awaiting payment."));
    }

    @KafkaListener(topics = PaymentCompletedEvent.TOPIC, groupId = "notification-service")
    public void onPaymentCompleted(String payload) throws Exception {
        PaymentCompletedEvent e = objectMapper.readValue(payload, PaymentCompletedEvent.class);
        store.record(Notification.of(e.getType(), e.getOrderId(),
                "Payment received — your order is confirmed."));
    }

    @KafkaListener(topics = PaymentFailedEvent.TOPIC, groupId = "notification-service")
    public void onPaymentFailed(String payload) throws Exception {
        PaymentFailedEvent e = objectMapper.readValue(payload, PaymentFailedEvent.class);
        store.record(Notification.of(e.getType(), e.getOrderId(),
                "Payment failed (" + e.getReason() + "). Your order was cancelled."));
    }

    @KafkaListener(topics = OrderDispatchedEvent.TOPIC, groupId = "notification-service")
    public void onOrderDispatched(String payload) throws Exception {
        OrderDispatchedEvent e = objectMapper.readValue(payload, OrderDispatchedEvent.class);
        store.record(Notification.of(e.getType(), e.getAggregateId(),
                "Your order is out for delivery with courier " + e.getCourierId() + "."));
    }

    @KafkaListener(topics = OrderDeliveredEvent.TOPIC, groupId = "notification-service")
    public void onOrderDelivered(String payload) throws Exception {
        OrderDeliveredEvent e = objectMapper.readValue(payload, OrderDeliveredEvent.class);
        store.record(Notification.of(e.getType(), e.getAggregateId(),
                "Your order has been delivered. Enjoy your meal!"));
    }
}
