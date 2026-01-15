package com.fooddelivery.controller;

import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.util.FileUploadUtil;
import com.fooddelivery.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
public class FileUploadController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadRestaurantImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = securityUtil.getUserIdFromAuthentication(authentication);
            Restaurant restaurant = restaurantRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

            String fileUrl = fileUploadUtil.uploadFile(file, "/restaurants");
            
            // Update restaurant image URL (if we add image field to Restaurant entity)
            // For now, just return the URL
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("url", fileUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/menu/items/{id}/upload-image")
    public ResponseEntity<Map<String, String>> uploadMenuItemImage(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = securityUtil.getUserIdFromAuthentication(authentication);
            Restaurant restaurant = restaurantRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

            MenuItem menuItem = menuItemRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new ResourceNotFoundException("Menu item does not belong to this restaurant");
            }

            String fileUrl = fileUploadUtil.uploadFile(file, "/menu-items");
            menuItem.setImage(fileUrl);
            menuItemRepository.save(menuItem);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("url", fileUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

