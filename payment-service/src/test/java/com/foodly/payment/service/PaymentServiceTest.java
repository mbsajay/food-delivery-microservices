package com.foodly.payment.service;

import com.foodly.common.event.OrderPlacedEvent;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.common.event.PaymentFailedEvent;
import com.foodly.common.event.DomainEvent;
import com.foodly.payment.domain.Payment;
import com.foodly.payment.domain.PaymentStatus;
import com.foodly.payment.gateway.PaymentGateway;
import com.foodly.payment.kafka.EventPublisher;
import com.foodly.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository payments;
    @Mock
    private PaymentGateway gateway;
    @Mock
    private EventPublisher events;

    @InjectMocks
    private PaymentService paymentService;

    private OrderPlacedEvent orderPlaced(UUID orderId, String amount) {
        return OrderPlacedEvent.builder()
                .orderId(orderId.toString())
                .totalAmount(new BigDecimal(amount))
                .currency("USD")
                .build();
    }

    @Test
    void processOrderPlaced_chargesAndEmitsCompletedOnApproval() {
        UUID orderId = UUID.randomUUID();
        when(payments.existsByOrderId(orderId)).thenReturn(false);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(UUID.randomUUID());
            }
            return p;
        });
        when(gateway.charge(eq(orderId), any(), eq("USD")))
                .thenReturn(PaymentGateway.Result.success("PG-123"));

        paymentService.processOrderPlaced(orderPlaced(orderId, "20.00"));

        ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(event.capture());
        assertThat(event.getValue()).isInstanceOf(PaymentCompletedEvent.class);
        PaymentCompletedEvent completed = (PaymentCompletedEvent) event.getValue();
        assertThat(completed.getOrderId()).isEqualTo(orderId.toString());
        assertThat(completed.getProviderReference()).isEqualTo("PG-123");
    }

    @Test
    void processOrderPlaced_emitsFailedWhenGatewayDeclines() {
        UUID orderId = UUID.randomUUID();
        when(payments.existsByOrderId(orderId)).thenReturn(false);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(UUID.randomUUID());
            }
            return p;
        });
        when(gateway.charge(any(), any(), any()))
                .thenReturn(PaymentGateway.Result.failure("DECLINED", "insufficient funds"));

        paymentService.processOrderPlaced(orderPlaced(orderId, "20.00"));

        ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(event.capture());
        assertThat(event.getValue()).isInstanceOf(PaymentFailedEvent.class);
        PaymentFailedEvent failed = (PaymentFailedEvent) event.getValue();
        assertThat(failed.getOrderId()).isEqualTo(orderId.toString());
        assertThat(failed.getReason()).isEqualTo("insufficient funds");
    }

    @Test
    void processOrderPlaced_isIdempotentForAnExistingOrder() {
        UUID orderId = UUID.randomUUID();
        when(payments.existsByOrderId(orderId)).thenReturn(true);

        paymentService.processOrderPlaced(orderPlaced(orderId, "20.00"));

        verify(payments, never()).save(any());
        verify(gateway, never()).charge(any(), any(), any());
        verify(events, never()).publish(any());
    }

    @Test
    void completed_buildsEventFromPayment() {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .amount(new BigDecimal("12.00"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .provider(PaymentGateway.PROVIDER)
                .providerReference("PG-9")
                .build();

        PaymentCompletedEvent event = PaymentService.completed(payment);

        assertThat(event.getPaymentId()).isEqualTo(payment.getId().toString());
        assertThat(event.getOrderId()).isEqualTo(payment.getOrderId().toString());
        assertThat(event.getAmount()).isEqualByComparingTo("12.00");
    }
}
