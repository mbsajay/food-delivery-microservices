package com.foodly.payment.repository;

import com.foodly.payment.domain.Payment;
import com.foodly.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, Instant cutoff);
}
