package com.fooddelivery.controller;

import com.fooddelivery.dto.PeakHourResponse;
import com.fooddelivery.dto.PopularItemResponse;
import com.fooddelivery.dto.RestaurantDashboardResponse;
import com.fooddelivery.service.RestaurantAnalyticsService;
import com.fooddelivery.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/restaurant/analytics")
public class RestaurantAnalyticsController {

    @Autowired
    private RestaurantAnalyticsService restaurantAnalyticsService;

    @Autowired
    private SecurityUtil securityUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @GetMapping("/dashboard")
    public ResponseEntity<RestaurantDashboardResponse> getDashboard(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        RestaurantDashboardResponse dashboard = restaurantAnalyticsService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/popular-items")
    public ResponseEntity<List<PopularItemResponse>> getPopularItems(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        List<PopularItemResponse> popularItems = restaurantAnalyticsService.getPopularItems(userId, limit);
        return ResponseEntity.ok(popularItems);
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenue(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        
        LocalDateTime start = startDate != null 
                ? LocalDateTime.parse(startDate, DATE_TIME_FORMATTER) 
                : null;
        LocalDateTime end = endDate != null 
                ? LocalDateTime.parse(endDate, DATE_TIME_FORMATTER) 
                : null;
        
        BigDecimal revenue = restaurantAnalyticsService.getRevenue(userId, start, end);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/peak-hours")
    public ResponseEntity<List<PeakHourResponse>> getPeakHours(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        List<PeakHourResponse> peakHours = restaurantAnalyticsService.getPeakHours(userId);
        return ResponseEntity.ok(peakHours);
    }
}

