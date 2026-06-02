package com.foodly.payment.web;

import com.foodly.common.api.ApiResponse;
import com.foodly.payment.dto.PaymentResponse;
import com.foodly.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Read-only payment lookup. Reached via the gateway as {@code /api/payments/**}. */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(PaymentResponse.from(paymentService.get(id)));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentResponse> getByOrder(@PathVariable UUID orderId) {
        return ApiResponse.ok(PaymentResponse.from(paymentService.getByOrder(orderId)));
    }
}
