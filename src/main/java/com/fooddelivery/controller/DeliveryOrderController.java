package com.fooddelivery.controller;

import com.fooddelivery.dto.OrderResponse;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/orders")
public class DeliveryOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/available")
    public ResponseEntity<Page<OrderResponse>> getAvailableOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAvailableOrdersForDelivery(pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        OrderResponse response = orderService.acceptDelivery(userId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> markOrderDelivered(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        OrderResponse response = orderService.markOrderDelivered(userId, id);
        return ResponseEntity.ok(response);
    }
}

