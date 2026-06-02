package com.foodly.restaurant.service;

import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.common.paging.PageResponse;
import com.foodly.restaurant.domain.MenuItem;
import com.foodly.restaurant.domain.Restaurant;
import com.foodly.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.foodly.restaurant.dto.RestaurantDtos.MenuItemRequest;
import com.foodly.restaurant.dto.RestaurantDtos.MenuItemResponse;
import com.foodly.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.foodly.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurants;

    @Transactional
    public RestaurantResponse create(UUID ownerId, CreateRestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .name(request.name())
                .description(request.description())
                .cuisine(request.cuisine())
                .city(request.city())
                .address(request.address())
                .active(true)
                .build();
        return RestaurantResponse.from(restaurants.save(restaurant));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse get(UUID id) {
        return RestaurantResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> search(String city, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Restaurant> result;
        if (StringUtils.hasText(city)) {
            result = restaurants.findByActiveTrueAndCityIgnoreCase(city, pageable);
        } else if (StringUtils.hasText(query)) {
            result = restaurants.findByActiveTrueAndNameContainingIgnoreCase(query, pageable);
        } else {
            result = restaurants.findByActiveTrue(pageable);
        }
        return PageResponse.of(
                result.getContent().stream().map(RestaurantResponse::summary).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Transactional
    public MenuItemResponse addMenuItem(UUID restaurantId, UUID ownerId, MenuItemRequest request) {
        Restaurant restaurant = find(restaurantId);
        assertOwner(restaurant, ownerId);
        MenuItem item = MenuItem.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .available(request.available())
                .build();
        restaurant.addMenuItem(item);
        restaurants.save(restaurant);
        return MenuItemResponse.from(item);
    }

    private Restaurant find(UUID id) {
        return restaurants.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Restaurant", id));
    }

    private static void assertOwner(Restaurant restaurant, UUID ownerId) {
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new com.foodly.common.exception.UnauthorizedException("Not the owner of this restaurant");
        }
    }
}
