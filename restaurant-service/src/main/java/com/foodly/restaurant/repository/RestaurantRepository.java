package com.foodly.restaurant.repository;

import com.foodly.restaurant.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Page<Restaurant> findByActiveTrue(Pageable pageable);

    Page<Restaurant> findByActiveTrueAndCityIgnoreCase(String city, Pageable pageable);

    Page<Restaurant> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
