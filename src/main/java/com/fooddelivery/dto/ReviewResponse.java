package com.fooddelivery.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long customerId;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

