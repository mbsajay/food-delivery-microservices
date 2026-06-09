package com.foodly.delivery.service;

import com.foodly.common.event.OrderDeliveredEvent;
import com.foodly.common.event.OrderDispatchedEvent;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.common.exception.ConflictException;
import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.delivery.domain.Delivery;
import com.foodly.delivery.domain.DeliveryStatus;
import com.foodly.delivery.kafka.EventPublisher;
import com.foodly.delivery.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveries;
    @Mock
    private EventPublisher events;

    @InjectMocks
    private DeliveryService deliveryService;

    private PaymentCompletedEvent paymentCompleted(UUID orderId) {
        return PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(orderId.toString())
                .build();
    }

    @Test
    void dispatchForPayment_assignsCourierAndEmitsDispatched() {
        UUID orderId = UUID.randomUUID();
        when(deliveries.existsByOrderId(orderId)).thenReturn(false);
        when(deliveries.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        deliveryService.dispatchForPayment(paymentCompleted(orderId));

        ArgumentCaptor<Delivery> saved = ArgumentCaptor.forClass(Delivery.class);
        verify(deliveries).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(DeliveryStatus.DISPATCHED);
        assertThat(saved.getValue().getCourierId()).isNotBlank();

        ArgumentCaptor<OrderDispatchedEvent> event = ArgumentCaptor.forClass(OrderDispatchedEvent.class);
        verify(events).publish(event.capture());
        assertThat(event.getValue().getOrderId()).isEqualTo(orderId.toString());
        assertThat(event.getValue().getCourierId()).isEqualTo(saved.getValue().getCourierId());
    }

    @Test
    void dispatchForPayment_isIdempotentWhenDeliveryExists() {
        UUID orderId = UUID.randomUUID();
        when(deliveries.existsByOrderId(orderId)).thenReturn(true);

        deliveryService.dispatchForPayment(paymentCompleted(orderId));

        verify(deliveries, never()).save(any());
        verify(events, never()).publish(any());
    }

    @Test
    void complete_marksDeliveredAndEmitsDeliveredEvent() {
        UUID orderId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .courierId("COURIER-ABCD1234")
                .status(DeliveryStatus.DISPATCHED)
                .build();
        when(deliveries.findByOrderId(orderId)).thenReturn(Optional.of(delivery));
        when(deliveries.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        deliveryService.complete(orderId, 5);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getDeliveredAt()).isNotNull();

        ArgumentCaptor<OrderDeliveredEvent> event = ArgumentCaptor.forClass(OrderDeliveredEvent.class);
        verify(events).publish(event.capture());
        assertThat(event.getValue().getOrderId()).isEqualTo(orderId.toString());
        assertThat(event.getValue().getCustomerRating()).isEqualTo(5);
    }

    @Test
    void complete_rejectsAlreadyDeliveredTrip() {
        UUID orderId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .status(DeliveryStatus.DELIVERED)
                .build();
        when(deliveries.findByOrderId(orderId)).thenReturn(Optional.of(delivery));

        assertThatThrownBy(() -> deliveryService.complete(orderId, null))
                .isInstanceOf(ConflictException.class);
        verify(events, never()).publish(any());
    }

    @Test
    void complete_throwsWhenDeliveryMissing() {
        UUID orderId = UUID.randomUUID();
        when(deliveries.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.complete(orderId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
