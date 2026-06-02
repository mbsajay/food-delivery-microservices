package com.foodly.payment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodly.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;

    public void publish(DomainEvent event) {
        try {
            kafka.send(event.getTopic(), event.getAggregateId(), objectMapper.writeValueAsString(event));
            log.debug("Published {} to {} for {}", event.getType(), event.getTopic(), event.getAggregateId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise event " + event.getType(), e);
        }
    }
}
