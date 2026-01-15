package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String image;
    private Boolean isVeg;
    private Boolean isAvailable;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long restaurantId;
    private String restaurantName;
}

