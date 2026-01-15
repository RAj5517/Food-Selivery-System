package com.fooddelivery.controller;

import com.fooddelivery.dto.RestaurantResponse;
import com.fooddelivery.dto.RestaurantUpdateRequest;
import com.fooddelivery.service.RestaurantService;
import com.fooddelivery.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/profile")
    public ResponseEntity<RestaurantResponse> getRestaurantProfile(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        RestaurantResponse response = restaurantService.getRestaurantByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<RestaurantResponse> updateRestaurantProfile(
            Authentication authentication,
            @Valid @RequestBody RestaurantUpdateRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        RestaurantResponse response = restaurantService.updateRestaurantProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/toggle-status")
    public ResponseEntity<RestaurantResponse> toggleRestaurantStatus(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        RestaurantResponse response = restaurantService.toggleRestaurantStatus(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteRestaurant(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        restaurantService.deleteRestaurant(userId);
        return ResponseEntity.noContent().build();
    }
}

