package com.foodly.delivery.web;

import com.foodly.common.api.ApiResponse;
import com.foodly.delivery.dto.DeliveryResponse;
import com.foodly.delivery.service.DeliveryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Delivery tracking + trip completion. Reached via the gateway as {@code /api/delivery/**}.
 * Dispatch happens automatically off {@code payment.completed}; completion is the courier
 * marking the trip done (which emits {@code order.delivered}).
 */
@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/order/{orderId}")
    public ApiResponse<DeliveryResponse> getByOrder(@PathVariable UUID orderId) {
        return ApiResponse.ok(DeliveryResponse.from(deliveryService.getByOrder(orderId)));
    }

    @PostMapping("/order/{orderId}/complete")
    @PreAuthorize("hasAnyRole('DELIVERY_AGENT', 'ADMIN')")
    public ApiResponse<DeliveryResponse> complete(@PathVariable UUID orderId,
                                                  @RequestParam(required = false) Integer rating) {
        return ApiResponse.ok(DeliveryResponse.from(deliveryService.complete(orderId, rating)));
    }
}
