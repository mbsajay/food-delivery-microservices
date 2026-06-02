package com.foodly.delivery.dto;

import com.foodly.delivery.domain.Delivery;
import com.foodly.delivery.domain.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
        UUID id, UUID orderId, String courierId, DeliveryStatus status,
        Instant dispatchedAt, Instant estimatedDeliveryAt, Instant deliveredAt) {

    public static DeliveryResponse from(Delivery d) {
        return new DeliveryResponse(d.getId(), d.getOrderId(), d.getCourierId(), d.getStatus(),
                d.getDispatchedAt(), d.getEstimatedDeliveryAt(), d.getDeliveredAt());
    }
}
