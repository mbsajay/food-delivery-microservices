package com.foodly.delivery.service;

import com.foodly.common.event.OrderDeliveredEvent;
import com.foodly.common.event.OrderDispatchedEvent;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.common.exception.ConflictException;
import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.delivery.domain.Delivery;
import com.foodly.delivery.domain.DeliveryStatus;
import com.foodly.delivery.kafka.EventPublisher;
import com.foodly.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private static final Duration ETA = Duration.ofMinutes(30);

    private final DeliveryRepository deliveries;
    private final EventPublisher events;

    /**
     * On {@code payment.completed}: assign a courier, record the dispatch and emit
     * {@code order.dispatched}. Idempotent on order id.
     */
    @Transactional
    public void dispatchForPayment(PaymentCompletedEvent payment) {
        UUID orderId = UUID.fromString(payment.getOrderId());
        if (deliveries.existsByOrderId(orderId)) {
            log.info("Delivery for order {} already exists — skipping dispatch", orderId);
            return;
        }
        String courierId = assignCourier();
        Instant now = Instant.now();
        deliveries.save(Delivery.builder()
                .orderId(orderId)
                .courierId(courierId)
                .status(DeliveryStatus.DISPATCHED)
                .dispatchedAt(now)
                .estimatedDeliveryAt(now.plus(ETA))
                .build());

        events.publish(OrderDispatchedEvent.builder()
                .orderId(orderId.toString())
                .courierId(courierId)
                .pickedUpAt(now)
                .estimatedDeliveryAt(now.plus(ETA))
                .build());
    }

    /** Completes the trip and emits {@code order.delivered}. */
    @Transactional
    public Delivery complete(UUID orderId, Integer rating) {
        Delivery delivery = deliveries.findByOrderId(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Delivery for order", orderId));
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new ConflictException("Delivery already completed");
        }
        Instant now = Instant.now();
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(now);
        deliveries.save(delivery);

        events.publish(OrderDeliveredEvent.builder()
                .orderId(orderId.toString())
                .courierId(delivery.getCourierId())
                .deliveredAt(now)
                .customerRating(rating)
                .build());
        return delivery;
    }

    @Transactional(readOnly = true)
    public Delivery getByOrder(UUID orderId) {
        return deliveries.findByOrderId(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Delivery for order", orderId));
    }

    /** Mock courier assignment — a real system would pick the nearest available agent. */
    private String assignCourier() {
        return "COURIER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
