package com.foodly.payment.gateway;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stand-in for an external payment processor. Charges synchronously and exposes a
 * status lookup that the reconciliation job uses to resolve drift. Deterministic so
 * the demo flow is repeatable: any positive amount succeeds.
 */
@Component
public class PaymentGateway {

    public static final String PROVIDER = "mock-pay";

    public Result charge(UUID orderId, BigDecimal amount, String currency) {
        if (amount == null || amount.signum() <= 0) {
            return Result.failure("AMOUNT_INVALID", "Charge amount must be positive");
        }
        return Result.success("PG-" + UUID.randomUUID());
    }

    /** Authoritative status for a previously initiated charge (used by reconciliation). */
    public Status status(String providerReference) {
        return providerReference == null ? Status.FAILED : Status.COMPLETED;
    }

    public enum Status {
        COMPLETED, FAILED, PENDING
    }

    public record Result(boolean approved, String providerReference, String reasonCode, String reason) {
        public static Result success(String reference) {
            return new Result(true, reference, null, null);
        }

        public static Result failure(String reasonCode, String reason) {
            return new Result(false, null, reasonCode, reason);
        }
    }
}
