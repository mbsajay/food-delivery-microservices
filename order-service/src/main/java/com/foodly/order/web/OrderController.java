package com.foodly.order.web;

import com.foodly.common.api.ApiResponse;
import com.foodly.common.paging.PageResponse;
import com.foodly.order.dto.OrderDtos.OrderResponse;
import com.foodly.order.dto.OrderDtos.PlaceOrderRequest;
import com.foodly.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Order placement and lookup. Reached via the gateway as {@code /api/orders/**}. */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> place(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.ok(orderService.place(subject(jwt), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(orderService.get(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> mine(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(orderService.listForCustomer(subject(jwt), page, Math.min(size, 100)));
    }

    private static UUID subject(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
