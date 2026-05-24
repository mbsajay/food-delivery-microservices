package com.foodly.common.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;
    private final String aggregateId;

    protected DomainEvent(String aggregateId) {
        this(UUID.randomUUID(), Instant.now(), aggregateId);
    }

    protected DomainEvent(UUID eventId, Instant occurredAt, String aggregateId) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.aggregateId = aggregateId;
    }

    @JsonIgnore
    public abstract String getType();

    @JsonIgnore
    public abstract String getTopic();
}
