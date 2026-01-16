package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDashboardResponse {
    private Long ordersToday;
    private BigDecimal revenueToday;
    private BigDecimal averageRating;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private List<PopularItemResponse> popularItems;
}

