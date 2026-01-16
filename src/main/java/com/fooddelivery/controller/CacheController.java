package com.fooddelivery.controller;

import com.fooddelivery.dto.CacheStatsResponse;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CacheStatsResponse>> getCacheStatistics() {
        List<CacheStatsResponse> statsList = new ArrayList<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (cache != null) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();
                CacheStats stats = nativeCache.stats();

                CacheStatsResponse response = new CacheStatsResponse();
                response.setCacheName(cacheName);
                response.setSize(nativeCache.estimatedSize());
                response.setHitCount(stats.hitCount());
                response.setMissCount(stats.missCount());
                response.setHitRate(stats.hitRate());
                response.setMissRate(stats.missRate());

                Map<String, Object> additionalStats = new HashMap<>();
                additionalStats.put("evictionCount", stats.evictionCount());
                additionalStats.put("loadCount", stats.loadCount());
                additionalStats.put("loadSuccessCount", stats.loadSuccessCount());
                additionalStats.put("loadFailureCount", stats.loadFailureCount());
                additionalStats.put("totalLoadTime", stats.totalLoadTime());
                response.setAdditionalStats(additionalStats);

                statsList.add(response);
            }
        });

        return ResponseEntity.ok(statsList);
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheManager.getCache(cacheName).clear();
        });

        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear/{cacheName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

