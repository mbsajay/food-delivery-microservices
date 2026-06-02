package com.foodly.restaurant.dto;

import com.foodly.restaurant.domain.MenuItem;
import com.foodly.restaurant.domain.Restaurant;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Request/response payloads for the restaurant catalog. Grouped to keep the DTO surface in one place. */
public final class RestaurantDtos {

    private RestaurantDtos() {
    }

    public record CreateRestaurantRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            @Size(max = 80) String cuisine,
            @Size(max = 80) String city,
            @Size(max = 250) String address) {
    }

    public record MenuItemRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            @Size(max = 80) String category,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            boolean available) {
    }

    public record MenuItemResponse(
            UUID id, String name, String description, String category, BigDecimal price, boolean available) {

        public static MenuItemResponse from(MenuItem item) {
            return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(),
                    item.getCategory(), item.getPrice(), item.isAvailable());
        }
    }

    public record RestaurantResponse(
            UUID id, UUID ownerId, String name, String description, String cuisine,
            String city, String address, boolean active, List<MenuItemResponse> menu) {

        public static RestaurantResponse from(Restaurant r) {
            return new RestaurantResponse(r.getId(), r.getOwnerId(), r.getName(), r.getDescription(),
                    r.getCuisine(), r.getCity(), r.getAddress(), r.isActive(),
                    r.getMenu().stream().map(MenuItemResponse::from).toList());
        }

        /** Summary projection without the menu, for list/search results. */
        public static RestaurantResponse summary(Restaurant r) {
            return new RestaurantResponse(r.getId(), r.getOwnerId(), r.getName(), r.getDescription(),
                    r.getCuisine(), r.getCity(), r.getAddress(), r.isActive(), List.of());
        }
    }
}
