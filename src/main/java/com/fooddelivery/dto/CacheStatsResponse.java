package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatsResponse {
    private String cacheName;
    private Long size;
    private Long hitCount;
    private Long missCount;
    private Double hitRate;
    private Double missRate;
    private Map<String, Object> additionalStats;
}

