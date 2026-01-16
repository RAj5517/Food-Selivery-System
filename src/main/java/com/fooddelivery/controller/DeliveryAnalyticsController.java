package com.fooddelivery.controller;

import com.fooddelivery.dto.DeliveryAnalyticsResponse;
import com.fooddelivery.service.DeliveryAnalyticsService;
import com.fooddelivery.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/analytics")
public class DeliveryAnalyticsController {

    @Autowired
    private DeliveryAnalyticsService deliveryAnalyticsService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/earnings")
    public ResponseEntity<DeliveryAnalyticsResponse> getEarnings(
            Authentication authentication,
            @RequestParam(required = false) String period) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        DeliveryAnalyticsResponse analytics = deliveryAnalyticsService.getAnalytics(userId, period);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/completed-deliveries")
    public ResponseEntity<Long> getCompletedDeliveries(
            Authentication authentication,
            @RequestParam(required = false) String period) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        DeliveryAnalyticsResponse analytics = deliveryAnalyticsService.getAnalytics(userId, period);
        return ResponseEntity.ok(analytics.getCompletedDeliveries());
    }

    @GetMapping("/average-delivery-time")
    public ResponseEntity<Double> getAverageDeliveryTime(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        DeliveryAnalyticsResponse analytics = deliveryAnalyticsService.getAnalytics(userId, "MONTHLY");
        return ResponseEntity.ok(analytics.getAverageDeliveryTimeMinutes());
    }
}

