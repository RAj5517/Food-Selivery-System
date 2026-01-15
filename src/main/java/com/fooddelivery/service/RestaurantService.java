package com.fooddelivery.service;

import com.fooddelivery.dto.RestaurantResponse;
import com.fooddelivery.dto.RestaurantUpdateRequest;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<RestaurantResponse> getAllRestaurants(String cuisine, String city, BigDecimal minRating, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findRestaurantsWithFilters(cuisine, city, minRating, pageable);
        return restaurants.map(this::convertToResponse);
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return convertToResponse(restaurant);
    }

    public Page<RestaurantResponse> getRestaurantsByCuisine(String cuisine, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findByCuisineIgnoreCase(cuisine, pageable);
        return restaurants.map(this::convertToResponse);
    }

    public Page<RestaurantResponse> getRestaurantsByCity(String city, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findByCity(city, pageable);
        return restaurants.map(this::convertToResponse);
    }

    public List<RestaurantResponse> getNearbyRestaurants(Double lat, Double longitude, Double radius) {
        List<Restaurant> restaurants = restaurantRepository.findNearbyRestaurants(lat, longitude, radius);
        return restaurants.stream().map(this::convertToResponse).toList();
    }

    @Transactional
    public RestaurantResponse updateRestaurantProfile(Long userId, RestaurantUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setLat(request.getLat());
        restaurant.setLongitude(request.getLongitude());
        if (request.getAvgPrepTime() != null) {
            restaurant.setAvgPrepTime(request.getAvgPrepTime());
        }

        restaurant = restaurantRepository.save(restaurant);
        return convertToResponse(restaurant);
    }

    @Transactional
    public RestaurantResponse toggleRestaurantStatus(Long userId) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setIsOpen(!restaurant.getIsOpen());
        restaurant = restaurantRepository.save(restaurant);
        return convertToResponse(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long userId) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        // Soft delete by deactivating user
        User user = restaurant.getUser();
        user.setIsActive(false);
        userRepository.save(user);
    }

    public RestaurantResponse getRestaurantByUserId(Long userId) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return convertToResponse(restaurant);
    }

    private RestaurantResponse convertToResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setCuisine(restaurant.getCuisine());
        response.setAddress(restaurant.getAddress());
        response.setLat(restaurant.getLat());
        response.setLongitude(restaurant.getLongitude());
        response.setIsOpen(restaurant.getIsOpen());
        response.setAvgPrepTime(restaurant.getAvgPrepTime());
        response.setRating(restaurant.getRating());
        response.setIsApproved(restaurant.getIsApproved());
        if (restaurant.getUser() != null) {
            response.setEmail(restaurant.getUser().getEmail());
            response.setPhone(restaurant.getUser().getPhone());
        }
        return response;
    }
}

