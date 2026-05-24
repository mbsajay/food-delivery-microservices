package com.foodly.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PaymentFailedEvent extends DomainEvent {

    public static final String TYPE = "payment.failed";
    public static final String TOPIC = "payment.failed";

    private final String paymentId;
    private final String orderId;
    private final String reasonCode;
    private final String reason;
    private final boolean retryable;

    @Builder
    @JsonCreator
    public PaymentFailedEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("aggregateId") String paymentId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("reasonCode") String reasonCode,
            @JsonProperty("reason") String reason,
            @JsonProperty("retryable") boolean retryable) {
        super(eventId == null ? UUID.randomUUID() : eventId,
              occurredAt == null ? Instant.now() : occurredAt,
              paymentId);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.reasonCode = reasonCode;
        this.reason = reason;
        this.retryable = retryable;
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
