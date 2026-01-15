package com.fooddelivery.controller;

import com.fooddelivery.dto.MenuItemResponse;
import com.fooddelivery.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant")
public class PublicMenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<Page<MenuItemResponse>> getRestaurantMenu(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Boolean isVeg,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MenuItemResponse> menuItems = menuService.getMenuItems(restaurantId, category, isVeg, isAvailable, pageable);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/menu/items/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        MenuItemResponse menuItem = menuService.getMenuItemById(id);
        return ResponseEntity.ok(menuItem);
    }
}

