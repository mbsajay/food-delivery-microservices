package com.foodly.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class OrderDispatchedEvent extends DomainEvent {

    public static final String TYPE = "order.dispatched";
    public static final String TOPIC = "order.dispatched";

    private final String orderId;
    private final String courierId;
    private final String restaurantId;
    private final Instant pickedUpAt;
    private final Instant estimatedDeliveryAt;

    @Builder
    @JsonCreator
    public OrderDispatchedEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("aggregateId") String orderId,
            @JsonProperty("courierId") String courierId,
            @JsonProperty("restaurantId") String restaurantId,
            @JsonProperty("pickedUpAt") Instant pickedUpAt,
            @JsonProperty("estimatedDeliveryAt") Instant estimatedDeliveryAt) {
        super(eventId == null ? UUID.randomUUID() : eventId,
              occurredAt == null ? Instant.now() : occurredAt,
              orderId);
        this.orderId = orderId;
        this.courierId = courierId;
        this.restaurantId = restaurantId;
        this.pickedUpAt = pickedUpAt;
        this.estimatedDeliveryAt = estimatedDeliveryAt;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
