package com.foodly.payment.dto;

import com.foodly.payment.domain.Payment;
import com.foodly.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id, UUID orderId, BigDecimal amount, String currency, PaymentStatus status,
        String provider, String providerReference, String reason, Instant createdAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getAmount(), p.getCurrency(), p.getStatus(),
                p.getProvider(), p.getProviderReference(), p.getReason(), p.getCreatedAt());
    }
}
