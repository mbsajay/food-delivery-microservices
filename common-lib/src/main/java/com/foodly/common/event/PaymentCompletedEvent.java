package com.foodly.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class PaymentCompletedEvent extends DomainEvent {

    public static final String TYPE = "payment.completed";
    public static final String TOPIC = "payment.completed";

    private final String paymentId;
    private final String orderId;
    private final BigDecimal amount;
    private final String currency;
    private final String provider;
    private final String providerReference;

    @Builder
    @JsonCreator
    public PaymentCompletedEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("aggregateId") String paymentId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("provider") String provider,
            @JsonProperty("providerReference") String providerReference) {
        super(eventId == null ? UUID.randomUUID() : eventId,
              occurredAt == null ? Instant.now() : occurredAt,
              paymentId);
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.provider = provider;
        this.providerReference = providerReference;
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
