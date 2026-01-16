package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeakHourResponse {
    private Integer hour; // 0-23
    private Long orderCount;
}

