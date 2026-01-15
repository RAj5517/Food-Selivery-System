package com.fooddelivery.repository;

import com.fooddelivery.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    // Find menu items by restaurant
    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    // Find available menu items by restaurant
    Page<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId, Pageable pageable);
    
    // Find menu items by restaurant and category
    Page<MenuItem> findByRestaurantIdAndCategoryId(Long restaurantId, Long categoryId, Pageable pageable);
    
    // Find menu items by restaurant and veg/non-veg
    Page<MenuItem> findByRestaurantIdAndIsVeg(Long restaurantId, Boolean isVeg, Pageable pageable);
    
    // Find menu items with filters
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:isVeg IS NULL OR m.isVeg = :isVeg) AND " +
           "(:isAvailable IS NULL OR m.isAvailable = :isAvailable)")
    Page<MenuItem> findMenuItemsWithFilters(@Param("restaurantId") Long restaurantId,
                                            @Param("categoryId") Long categoryId,
                                            @Param("isVeg") Boolean isVeg,
                                            @Param("isAvailable") Boolean isAvailable,
                                            Pageable pageable);
    
    // Find all menu items by restaurant (no pagination for internal use)
    List<MenuItem> findByRestaurantId(Long restaurantId);
    
    // Check if menu item belongs to restaurant
    Boolean existsByIdAndRestaurantId(Long menuItemId, Long restaurantId);
}

