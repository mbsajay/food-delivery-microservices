package com.foodly.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodly.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Serialises {@link DomainEvent}s to JSON and publishes them keyed by aggregate id.
 * JSON-as-string (rather than typed JsonSerializer) keeps producer/consumer wiring
 * symmetric and free of type-header coupling across services.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;

    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafka.send(event.getTopic(), event.getAggregateId(), payload);
            log.debug("Published {} to {} for {}", event.getType(), event.getTopic(), event.getAggregateId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise event " + event.getType(), e);
        }
    }
}
