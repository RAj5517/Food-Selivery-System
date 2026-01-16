package com.fooddelivery.service;

import com.fooddelivery.dto.PeakHourResponse;
import com.fooddelivery.dto.PopularItemResponse;
import com.fooddelivery.dto.RestaurantDashboardResponse;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public RestaurantDashboardResponse getDashboard(Long userId) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        RestaurantDashboardResponse response = new RestaurantDashboardResponse();

        // Orders today
        Long ordersToday = orderRepository.countOrdersTodayByRestaurant(restaurant.getId());
        response.setOrdersToday(ordersToday);

        // Revenue today
        BigDecimal revenueToday = orderRepository.calculateRevenueTodayByRestaurant(restaurant.getId());
        response.setRevenueToday(revenueToday);

        // Average rating
        BigDecimal averageRating = reviewRepository.calculateAverageRatingByRestaurantId(restaurant.getId())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        response.setAverageRating(averageRating);

        // Total orders (all time)
        Long totalOrders = orderRepository.countByRestaurantIdAndStatus(restaurant.getId(), null) 
                + orderRepository.countByRestaurantIdAndStatus(restaurant.getId(), com.fooddelivery.model.Order.OrderStatus.DELIVERED);
        response.setTotalOrders(totalOrders);

        // Total revenue (all time)
        BigDecimal totalRevenue = orderRepository.calculateRevenueByRestaurantAndDateRange(
                restaurant.getId(),
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.now()
        );
        response.setTotalRevenue(totalRevenue);

        // Popular items (last 30 days)
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<Object[]> popularItemsData = orderRepository.findPopularItemsByRestaurant(
                restaurant.getId(),
                startDate,
                endDate,
                PageRequest.of(0, 10)
        );

        List<PopularItemResponse> popularItems = popularItemsData.stream().map(row -> {
            Long menuItemId = ((Number) row[0]).longValue();
            String menuItemName = (String) row[1];
            Long orderCount = ((Number) row[2]).longValue();
            Long totalQuantity = ((Number) row[3]).longValue();
            
            return new PopularItemResponse(menuItemId, menuItemName, orderCount, totalQuantity);
        }).collect(Collectors.toList());

        response.setPopularItems(popularItems);

        return response;
    }

    public List<PopularItemResponse> getPopularItems(Long userId, int limit) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> results = orderRepository.findPopularItemsByRestaurant(
                restaurant.getId(),
                startDate,
                endDate,
                PageRequest.of(0, limit)
        );

        return results.stream().map(row -> {
            Long menuItemId = ((Number) row[0]).longValue();
            String menuItemName = (String) row[1];
            Long orderCount = ((Number) row[2]).longValue();
            Long totalQuantity = ((Number) row[3]).longValue();
            
            return new PopularItemResponse(menuItemId, menuItemName, orderCount, totalQuantity);
        }).collect(Collectors.toList());
    }

    public BigDecimal getRevenue(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (startDate == null && endDate == null) {
            return orderRepository.calculateRevenueByRestaurantAndDateRange(
                    restaurant.getId(),
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now()
            );
        } else if (startDate == null) {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        } else if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        return orderRepository.calculateRevenueByRestaurantAndDateRange(
                restaurant.getId(),
                startDate,
                endDate
        );
    }

    public List<PeakHourResponse> getPeakHours(Long userId) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> results = orderRepository.findPeakHoursByRestaurant(
                restaurant.getId(),
                startDate,
                endDate
        );

        return results.stream().map(row -> {
            Integer hour = ((Number) row[0]).intValue();
            Long orderCount = ((Number) row[1]).longValue();
            
            return new PeakHourResponse(hour, orderCount);
        }).collect(Collectors.toList());
    }
}

