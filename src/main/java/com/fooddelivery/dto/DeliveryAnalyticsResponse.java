package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAnalyticsResponse {
    private Long completedDeliveries;
    private BigDecimal totalEarnings;
    private BigDecimal averageEarningPerDelivery;
    private Double averageDeliveryTimeMinutes; // Can be null if no data
    private Long totalOrdersAssigned;
}

