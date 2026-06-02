package com.foodly.payment.repository;

import com.foodly.payment.domain.PaymentReconLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentReconLogRepository extends JpaRepository<PaymentReconLog, UUID> {
}
