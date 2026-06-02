package com.foodly.delivery.repository;

import com.foodly.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
