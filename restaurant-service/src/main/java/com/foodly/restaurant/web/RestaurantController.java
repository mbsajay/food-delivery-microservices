package com.foodly.restaurant.web;

import com.foodly.common.api.ApiResponse;
import com.foodly.common.paging.PageResponse;
import com.foodly.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.foodly.restaurant.dto.RestaurantDtos.MenuItemRequest;
import com.foodly.restaurant.dto.RestaurantDtos.MenuItemResponse;
import com.foodly.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.foodly.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

/**
 * Catalog API. Reads are public; creating restaurants and menu items requires a
 * RESTAURANT_OWNER (or ADMIN). Reached via the gateway as {@code /api/restaurants/**}.
 */
@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ApiResponse<PageResponse<RestaurantResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(restaurantService.search(city, q, page, Math.min(size, 100)));
    }

    @GetMapping("/{id}")
    public ApiResponse<RestaurantResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(restaurantService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ApiResponse<RestaurantResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CreateRestaurantRequest request) {
        return ApiResponse.ok(restaurantService.create(subject(jwt), request));
    }

    @PostMapping("/{id}/menu")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ApiResponse<MenuItemResponse> addMenuItem(@AuthenticationPrincipal Jwt jwt,
                                                     @PathVariable UUID id,
                                                     @Valid @RequestBody MenuItemRequest request) {
        return ApiResponse.ok(restaurantService.addMenuItem(id, subject(jwt), request));
    }

    private static UUID subject(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
