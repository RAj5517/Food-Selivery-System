package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerResponse {
    private Long customerId;
    private String customerName;
    private Long orderCount;
    private BigDecimal totalSpent;
}

