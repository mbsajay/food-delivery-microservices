package com.foodly.order.domain;

/**
 * Order lifecycle. Happy path:
 * {@code PENDING_PAYMENT → CONFIRMED → OUT_FOR_DELIVERY → DELIVERED}.
 * {@code CANCELLED} is reachable from {@code PENDING_PAYMENT} on payment failure/timeout.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
