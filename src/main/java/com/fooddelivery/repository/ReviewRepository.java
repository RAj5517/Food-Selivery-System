package com.fooddelivery.repository;

import com.fooddelivery.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByOrderId(Long orderId);
    
    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    Page<Review> findByCustomerId(Long customerId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Double calculateAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Long countByRestaurantId(@Param("restaurantId") Long restaurantId);
}

