package com.foodly.order.service;

import com.foodly.common.event.OrderPlacedEvent;
import com.foodly.order.domain.Order;
import com.foodly.order.domain.OrderStatus;
import com.foodly.order.dto.OrderDtos.LineItemRequest;
import com.foodly.order.dto.OrderDtos.OrderResponse;
import com.foodly.order.dto.OrderDtos.PlaceOrderRequest;
import com.foodly.order.kafka.EventPublisher;
import com.foodly.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orders;
    @Mock
    private EventPublisher events;

    @InjectMocks
    private OrderService orderService;

    @Test
    void place_sumsLineTotalsEmitsEventAndStartsPendingPayment() {
        UUID customerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        when(orders.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        PlaceOrderRequest request = new PlaceOrderRequest(restaurantId, "usd", List.of(
                new LineItemRequest(UUID.randomUUID(), "Margherita", 2, new BigDecimal("5.00")),
                new LineItemRequest(UUID.randomUUID(), "Cola", 1, new BigDecimal("3.50"))));

        OrderResponse response = orderService.place(customerId, request);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.totalAmount()).isEqualByComparingTo("13.50");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.items()).hasSize(2);

        ArgumentCaptor<OrderPlacedEvent> event = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(events).publish(event.capture());
        assertThat(event.getValue().getAggregateId()).isEqualTo(response.id().toString());
        assertThat(event.getValue().getCustomerId()).isEqualTo(customerId.toString());
        assertThat(event.getValue().getTotalAmount()).isEqualByComparingTo("13.50");
        assertThat(event.getValue().getItems()).hasSize(2);
    }

    @Test
    void markConfirmed_movesPendingPaymentToConfirmedAndRecordsPayment() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.PENDING_PAYMENT).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        orderService.markConfirmed(orderId, paymentId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getPaymentId()).isEqualTo(paymentId);
        verify(orders).save(order);
    }

    @Test
    void markConfirmed_isIgnoredWhenStatusIsNotPendingPayment() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.DELIVERED).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        orderService.markConfirmed(orderId, UUID.randomUUID());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orders, never()).save(any());
    }

    @Test
    void markDelivered_requiresOutForDeliveryFirst() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.OUT_FOR_DELIVERY).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        orderService.markDelivered(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orders).save(order);
    }

    @Test
    void transition_isIgnoredWhenOrderIsMissing() {
        UUID orderId = UUID.randomUUID();
        when(orders.findById(orderId)).thenReturn(Optional.empty());

        orderService.markCancelled(orderId);

        verify(orders, never()).save(any());
    }
}
