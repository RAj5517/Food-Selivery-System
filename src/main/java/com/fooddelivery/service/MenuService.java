package com.fooddelivery.service;

import com.fooddelivery.dto.MenuItemRequest;
import com.fooddelivery.dto.MenuItemResponse;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Category;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.CategoryRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantUserId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findByUserId(restaurantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setCategory(category);
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setIsVeg(request.getIsVeg());
        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        menuItem.setDescription(request.getDescription());

        menuItem = menuItemRepository.save(menuItem);
        return convertToResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long restaurantUserId, Long menuItemId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findByUserId(restaurantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));

        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
            throw new ResourceNotFoundException("Menu item does not belong to this restaurant");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(category);
        menuItem.setIsVeg(request.getIsVeg());
        menuItem.setIsAvailable(request.getIsAvailable());
        menuItem.setDescription(request.getDescription());

        menuItem = menuItemRepository.save(menuItem);
        return convertToResponse(menuItem);
    }

    @Transactional
    public void deleteMenuItem(Long restaurantUserId, Long menuItemId) {
        Restaurant restaurant = restaurantRepository.findByUserId(restaurantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));

        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
            throw new ResourceNotFoundException("Menu item does not belong to this restaurant");
        }

        menuItemRepository.delete(menuItem);
    }

    public Page<MenuItemResponse> getMenuItems(Long restaurantId, Long categoryId, Boolean isVeg, Boolean isAvailable, Pageable pageable) {
        Page<MenuItem> menuItems = menuItemRepository.findMenuItemsWithFilters(restaurantId, categoryId, isVeg, isAvailable, pageable);
        return menuItems.map(this::convertToResponse);
    }

    public MenuItemResponse getMenuItemById(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));
        return convertToResponse(menuItem);
    }

    private MenuItemResponse convertToResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setPrice(menuItem.getPrice());
        response.setImage(menuItem.getImage());
        response.setIsVeg(menuItem.getIsVeg());
        response.setIsAvailable(menuItem.getIsAvailable());
        response.setDescription(menuItem.getDescription());
        response.setCategoryId(menuItem.getCategory().getId());
        response.setCategoryName(menuItem.getCategory().getName());
        response.setRestaurantId(menuItem.getRestaurant().getId());
        response.setRestaurantName(menuItem.getRestaurant().getName());
        return response;
    }
}

