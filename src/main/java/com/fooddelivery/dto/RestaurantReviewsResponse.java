package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReviewsResponse {
    private Long restaurantId;
    private String restaurantName;
    private BigDecimal averageRating;
    private Long totalReviews;
    private List<ReviewResponse> reviews;
}

