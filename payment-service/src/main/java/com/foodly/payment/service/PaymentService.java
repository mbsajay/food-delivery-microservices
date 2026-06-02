package com.foodly.payment.service;

import com.foodly.common.event.OrderPlacedEvent;
import com.foodly.common.event.PaymentCompletedEvent;
import com.foodly.common.event.PaymentFailedEvent;
import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.payment.domain.Payment;
import com.foodly.payment.domain.PaymentStatus;
import com.foodly.payment.gateway.PaymentGateway;
import com.foodly.payment.kafka.EventPublisher;
import com.foodly.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final EventPublisher events;

    /**
     * Processes an {@code order.placed} event: charges the gateway and emits
     * {@code payment.completed} or {@code payment.failed}. Idempotent on order id so
     * a redelivered event does not double-charge.
     */
    @Transactional
    public void processOrderPlaced(OrderPlacedEvent order) {
        UUID orderId = UUID.fromString(order.getAggregateId());
        if (payments.existsByOrderId(orderId)) {
            log.info("Payment for order {} already exists — skipping", orderId);
            return;
        }

        String currency = order.getCurrency() == null ? "USD" : order.getCurrency();
        Payment payment = payments.save(Payment.builder()
                .orderId(orderId)
                .amount(order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount())
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .provider(PaymentGateway.PROVIDER)
                .build());

        PaymentGateway.Result result = gateway.charge(orderId, payment.getAmount(), currency);
        if (result.approved()) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProviderReference(result.providerReference());
            payments.save(payment);
            events.publish(completed(payment));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setReasonCode(result.reasonCode());
            payment.setReason(result.reason());
            payments.save(payment);
            events.publish(failed(payment));
        }
    }

    @Transactional(readOnly = true)
    public Payment get(UUID id) {
        return payments.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    @Transactional(readOnly = true)
    public Payment getByOrder(UUID orderId) {
        return payments.findByOrderId(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment for order", orderId));
    }

    public static PaymentCompletedEvent completed(Payment payment) {
        return PaymentCompletedEvent.builder()
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrderId().toString())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .provider(payment.getProvider())
                .providerReference(payment.getProviderReference())
                .build();
    }

    public static PaymentFailedEvent failed(Payment payment) {
        return PaymentFailedEvent.builder()
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrderId().toString())
                .reasonCode(payment.getReasonCode())
                .reason(payment.getReason())
                .retryable(false)
                .build();
    }
}
