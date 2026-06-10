package com.foodly.restaurant.service;

import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.common.exception.UnauthorizedException;
import com.foodly.restaurant.domain.MenuItem;
import com.foodly.restaurant.domain.Restaurant;
import com.foodly.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.foodly.restaurant.dto.RestaurantDtos.MenuItemRequest;
import com.foodly.restaurant.dto.RestaurantDtos.MenuItemResponse;
import com.foodly.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.foodly.restaurant.repository.MenuItemRepository;
import com.foodly.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurants;
    @Mock
    private MenuItemRepository menuItems;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void create_persistsActiveRestaurantOwnedByCaller() {
        UUID ownerId = UUID.randomUUID();
        when(restaurants.save(any(Restaurant.class))).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        RestaurantResponse response = restaurantService.create(ownerId,
                new CreateRestaurantRequest("Trattoria", "Italian classics", "Italian", "Pune", "MG Road"));

        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.name()).isEqualTo("Trattoria");
        assertThat(response.active()).isTrue();
    }

    @Test
    void addMenuItem_succeedsForOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = Restaurant.builder().id(restaurantId).ownerId(ownerId).name("Trattoria").active(true).build();
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItems.save(any(MenuItem.class))).thenAnswer(inv -> {
            MenuItem m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        MenuItemResponse item = restaurantService.addMenuItem(restaurantId, ownerId,
                new MenuItemRequest("Pizza", "Wood-fired", "Mains", new BigDecimal("9.50"), true));

        assertThat(item.id()).isNotNull();
        assertThat(item.name()).isEqualTo("Pizza");
        assertThat(item.price()).isEqualByComparingTo("9.50");
        assertThat(restaurant.getMenu()).hasSize(1);
    }

    @Test
    void addMenuItem_rejectsNonOwner() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = Restaurant.builder().id(restaurantId).ownerId(UUID.randomUUID()).name("Trattoria").active(true).build();
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.addMenuItem(restaurantId, UUID.randomUUID(),
                new MenuItemRequest("Pizza", null, null, new BigDecimal("9.50"), true)))
                .isInstanceOf(UnauthorizedException.class);

        verify(restaurants, never()).save(any());
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(restaurants.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
