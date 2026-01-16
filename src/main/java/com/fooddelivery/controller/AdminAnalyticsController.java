package com.fooddelivery.controller;

import com.fooddelivery.dto.AdminAnalyticsOverviewResponse;
import com.fooddelivery.dto.OrderTrendResponse;
import com.fooddelivery.dto.TopCustomerResponse;
import com.fooddelivery.dto.TopRestaurantResponse;
import com.fooddelivery.service.AdminAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @GetMapping("/overview")
    public ResponseEntity<AdminAnalyticsOverviewResponse> getOverview() {
        AdminAnalyticsOverviewResponse response = adminAnalyticsService.getOverview();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders-trend")
    public ResponseEntity<List<OrderTrendResponse>> getOrdersTrend(
            @RequestParam(defaultValue = "daily") String period) {
        List<OrderTrendResponse> trends = adminAnalyticsService.getOrdersTrend(period);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenue(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = startDate != null 
                ? LocalDateTime.parse(startDate, DATE_TIME_FORMATTER) 
                : null;
        LocalDateTime end = endDate != null 
                ? LocalDateTime.parse(endDate, DATE_TIME_FORMATTER) 
                : null;
        
        BigDecimal revenue = adminAnalyticsService.getRevenue(start, end);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/top-restaurants")
    public ResponseEntity<List<TopRestaurantResponse>> getTopRestaurants(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopRestaurantResponse> topRestaurants = adminAnalyticsService.getTopRestaurants(limit);
        return ResponseEntity.ok(topRestaurants);
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerResponse>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopCustomerResponse> topCustomers = adminAnalyticsService.getTopCustomers(limit);
        return ResponseEntity.ok(topCustomers);
    }
}

