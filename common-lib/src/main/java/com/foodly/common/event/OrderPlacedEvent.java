package com.foodly.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderPlacedEvent extends DomainEvent {

    public static final String TYPE = "order.placed";
    public static final String TOPIC = "order.placed";

    private final String customerId;
    private final String restaurantId;
    private final BigDecimal totalAmount;
    private final String currency;
    private final List<LineItem> items;

    @Builder
    @JsonCreator
    public OrderPlacedEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("aggregateId") String orderId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("restaurantId") String restaurantId,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("currency") String currency,
            @JsonProperty("items") List<LineItem> items) {
        super(eventId == null ? UUID.randomUUID() : eventId,
              occurredAt == null ? Instant.now() : occurredAt,
              orderId);
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Getter
    @Builder
    public static class LineItem {
        private final String menuItemId;
        private final String name;
        private final int quantity;
        private final BigDecimal unitPrice;

        @JsonCreator
        public LineItem(
                @JsonProperty("menuItemId") String menuItemId,
                @JsonProperty("name") String name,
                @JsonProperty("quantity") int quantity,
                @JsonProperty("unitPrice") BigDecimal unitPrice) {
            this.menuItemId = menuItemId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
