package com.foodly.notification.model;

import java.time.Instant;

/**
 * A delivered notification. notification-service is intentionally stateless (it is not
 * in the Postgres multi-DB list) — these are kept in an in-memory ring buffer for
 * inspection during the demo rather than persisted.
 */
public record Notification(
        String type,
        String aggregateId,
        String channel,
        String message,
        Instant createdAt) {

    public static Notification of(String type, String aggregateId, String message) {
        return new Notification(type, aggregateId, "LOG", message, Instant.now());
    }
}
