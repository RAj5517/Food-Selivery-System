package com.fooddelivery.controller;

import com.fooddelivery.dto.RestaurantResponse;
import com.fooddelivery.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class PublicRestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getAllRestaurants(
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RestaurantResponse> restaurants = restaurantService.getAllRestaurants(cuisine, city, minRating, pageable);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/cuisine/{cuisine}")
    public ResponseEntity<Page<RestaurantResponse>> getRestaurantsByCuisine(
            @PathVariable String cuisine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RestaurantResponse> restaurants = restaurantService.getRestaurantsByCuisine(cuisine, pageable);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<Page<RestaurantResponse>> getRestaurantsByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RestaurantResponse> restaurants = restaurantService.getRestaurantsByCity(city, pageable);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<RestaurantResponse>> getNearbyRestaurants(
            @RequestParam Double lat,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius) {
        List<RestaurantResponse> restaurants = restaurantService.getNearbyRestaurants(lat, longitude, radius);
        return ResponseEntity.ok(restaurants);
    }
}

