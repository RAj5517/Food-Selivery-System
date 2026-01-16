package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularItemResponse {
    private Long menuItemId;
    private String menuItemName;
    private Long orderCount;
    private Long totalQuantity;
}

