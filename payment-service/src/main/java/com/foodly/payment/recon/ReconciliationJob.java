package com.foodly.payment.recon;

import com.foodly.payment.domain.Payment;
import com.foodly.payment.domain.PaymentReconLog;
import com.foodly.payment.domain.PaymentStatus;
import com.foodly.payment.gateway.PaymentGateway;
import com.foodly.payment.kafka.EventPublisher;
import com.foodly.payment.repository.PaymentReconLogRepository;
import com.foodly.payment.repository.PaymentRepository;
import com.foodly.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Curriculum-required reconciliation. Every 15 minutes it finds payments still
 * {@code PENDING} after 5 minutes, asks the gateway for the authoritative status,
 * corrects local drift, re-emits the matching event, and writes an audit row to
 * {@code payment_recon_log}. This is the safety net behind the synchronous charge —
 * not a replacement for it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationJob {

    private static final Duration STALE_AFTER = Duration.ofMinutes(5);

    private final PaymentRepository payments;
    private final PaymentReconLogRepository reconLog;
    private final PaymentGateway gateway;
    private final EventPublisher events;

    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT1M")
    @Transactional
    public void reconcile() {
        Instant cutoff = Instant.now().minus(STALE_AFTER);
        List<Payment> stale = payments.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, cutoff);
        if (stale.isEmpty()) {
            return;
        }
        log.info("Reconciling {} stale PENDING payment(s)", stale.size());
        for (Payment payment : stale) {
            PaymentGateway.Status status = gateway.status(payment.getProviderReference());
            switch (status) {
                case COMPLETED -> resolve(payment, PaymentStatus.COMPLETED,
                        "gateway reports COMPLETED", PaymentService.completed(payment));
                case FAILED -> {
                    payment.setReasonCode("RECON_FAILED");
                    payment.setReason("gateway reports FAILED");
                    resolve(payment, PaymentStatus.FAILED,
                            "gateway reports FAILED", PaymentService.failed(payment));
                }
                case PENDING -> log.info("Payment {} still PENDING at gateway", payment.getId());
            }
        }
    }

    private void resolve(Payment payment, PaymentStatus newStatus, String detail,
                         com.foodly.common.event.DomainEvent event) {
        String previous = payment.getStatus().name();
        payment.setStatus(newStatus);
        payments.save(payment);
        reconLog.save(PaymentReconLog.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .previousStatus(previous)
                .newStatus(newStatus.name())
                .detail(detail)
                .build());
        events.publish(event);
        log.info("Reconciled payment {} {}→{} ({})", payment.getId(), previous, newStatus, detail);
    }
}
