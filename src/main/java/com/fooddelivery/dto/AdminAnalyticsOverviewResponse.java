package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsOverviewResponse {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long activeUsers;
    private Long activeRestaurants;
    private Long totalCustomers;
    private Long totalDeliveryPartners;
    private Long pendingOrders;
    private Long completedOrders;
}

