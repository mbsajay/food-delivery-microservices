package com.foodly.order.service;

import com.foodly.common.event.OrderPlacedEvent;
import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.common.paging.PageResponse;
import com.foodly.order.domain.Order;
import com.foodly.order.domain.OrderItem;
import com.foodly.order.domain.OrderStatus;
import com.foodly.order.dto.OrderDtos.OrderResponse;
import com.foodly.order.dto.OrderDtos.PlaceOrderRequest;
import com.foodly.order.kafka.EventPublisher;
import com.foodly.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orders;
    private final EventPublisher events;

    @Transactional
    public OrderResponse place(UUID customerId, PlaceOrderRequest request) {
        String currency = request.currency() == null ? "USD" : request.currency().toUpperCase();
        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.restaurantId())
                .status(OrderStatus.PENDING_PAYMENT)
                .currency(currency)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (var line : request.items()) {
            OrderItem item = OrderItem.builder()
                    .menuItemId(line.menuItemId())
                    .name(line.name())
                    .quantity(line.quantity())
                    .unitPrice(line.unitPrice())
                    .build();
            order.addItem(item);
            total = total.add(item.lineTotal());
        }
        order.setTotalAmount(total);
        Order saved = orders.save(order);

        events.publish(OrderPlacedEvent.builder()
                .orderId(saved.getId().toString())
                .customerId(customerId.toString())
                .restaurantId(saved.getRestaurantId().toString())
                .totalAmount(total)
                .currency(currency)
                .items(saved.getItems().stream()
                        .map(i -> OrderPlacedEvent.LineItem.builder()
                                .menuItemId(i.getMenuItemId().toString())
                                .name(i.getName())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .build())
                        .toList())
                .build());

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return OrderResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listForCustomer(UUID customerId, int page, int size) {
        Page<Order> result = orders.findByCustomerId(customerId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(
                result.getContent().stream().map(OrderResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }

    /** payment.completed → CONFIRMED. */
    @Transactional
    public void markConfirmed(UUID orderId, UUID paymentId) {
        transition(orderId, OrderStatus.PENDING_PAYMENT, OrderStatus.CONFIRMED, order -> order.setPaymentId(paymentId));
    }

    /** payment.failed → CANCELLED. */
    @Transactional
    public void markCancelled(UUID orderId) {
        transition(orderId, OrderStatus.PENDING_PAYMENT, OrderStatus.CANCELLED, order -> { });
    }

    /** order.dispatched → OUT_FOR_DELIVERY. */
    @Transactional
    public void markOutForDelivery(UUID orderId, String courierId) {
        transition(orderId, OrderStatus.CONFIRMED, OrderStatus.OUT_FOR_DELIVERY, order -> order.setCourierId(courierId));
    }

    /** order.delivered → DELIVERED. */
    @Transactional
    public void markDelivered(UUID orderId) {
        transition(orderId, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, order -> { });
    }

    private void transition(UUID orderId, OrderStatus expected, OrderStatus next, java.util.function.Consumer<Order> mutator) {
        Order order = orders.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Ignoring transition to {} — order {} not found", next, orderId);
            return;
        }
        if (order.getStatus() != expected) {
            log.warn("Ignoring transition {}→{} for order {} (current status {})",
                    expected, next, orderId, order.getStatus());
            return;
        }
        mutator.accept(order);
        order.setStatus(next);
        orders.save(order);
        log.info("Order {} {}→{} at {}", orderId, expected, next, Instant.now());
    }

    private Order find(UUID id) {
        return orders.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Order", id));
    }
}
