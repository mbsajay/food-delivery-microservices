package com.foodly.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class OrderDeliveredEvent extends DomainEvent {

    public static final String TYPE = "order.delivered";
    public static final String TOPIC = "order.delivered";

    private final String orderId;
    private final String courierId;
    private final Instant deliveredAt;
    private final Integer customerRating;

    @Builder
    @JsonCreator
    public OrderDeliveredEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("aggregateId") String orderId,
            @JsonProperty("courierId") String courierId,
            @JsonProperty("deliveredAt") Instant deliveredAt,
            @JsonProperty("customerRating") Integer customerRating) {
        super(eventId == null ? UUID.randomUUID() : eventId,
              occurredAt == null ? Instant.now() : occurredAt,
              orderId);
        this.orderId = orderId;
        this.courierId = courierId;
        this.deliveredAt = deliveredAt;
        this.customerRating = customerRating;
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
