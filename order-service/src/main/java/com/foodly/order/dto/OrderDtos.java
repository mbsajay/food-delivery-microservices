package com.foodly.order.dto;

import com.foodly.order.domain.Order;
import com.foodly.order.domain.OrderItem;
import com.foodly.order.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record PlaceOrderRequest(
            @NotNull UUID restaurantId,
            @Size(min = 3, max = 3) String currency,
            @NotEmpty @Valid List<LineItemRequest> items) {
    }

    public record LineItemRequest(
            @NotNull UUID menuItemId,
            @Size(max = 150) String name,
            @Min(1) int quantity,
            @NotNull @DecimalMin("0.0") BigDecimal unitPrice) {
    }

    public record OrderItemResponse(UUID menuItemId, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getMenuItemId(), item.getName(), item.getQuantity(),
                    item.getUnitPrice(), item.lineTotal());
        }
    }

    public record OrderResponse(
            UUID id, UUID customerId, UUID restaurantId, OrderStatus status,
            BigDecimal totalAmount, String currency, UUID paymentId, String courierId,
            List<OrderItemResponse> items, Instant createdAt) {

        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getCustomerId(), order.getRestaurantId(),
                    order.getStatus(), order.getTotalAmount(), order.getCurrency(), order.getPaymentId(),
                    order.getCourierId(), order.getItems().stream().map(OrderItemResponse::from).toList(),
                    order.getCreatedAt());
        }
    }
}
