package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopRestaurantResponse {
    private Long restaurantId;
    private String restaurantName;
    private Long orderCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
}

