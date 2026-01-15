package com.fooddelivery.controller;

import com.fooddelivery.dto.MenuItemRequest;
import com.fooddelivery.dto.MenuItemResponse;
import com.fooddelivery.service.MenuService;
import com.fooddelivery.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/items")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            Authentication authentication,
            @Valid @RequestBody MenuItemRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        MenuItemResponse response = menuService.addMenuItem(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        MenuItemResponse response = menuService.updateMenuItem(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteMenuItem(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        menuService.deleteMenuItem(userId, id);
        return ResponseEntity.noContent().build();
    }
}

