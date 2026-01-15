package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String cuisine;
    private String address;
    private Double lat;
    private Double longitude;
    private Boolean isOpen;
    private Integer avgPrepTime;
    private BigDecimal rating;
    private Boolean isApproved;
    private String email;
    private String phone;
}

