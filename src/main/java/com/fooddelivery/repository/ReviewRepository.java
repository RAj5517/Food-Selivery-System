package com.fooddelivery.repository;

import com.fooddelivery.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrderId(Long orderId);
    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);
    List<Review> findByCustomerId(Long customerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Optional<BigDecimal> calculateAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

    Long countByRestaurantId(Long restaurantId);
}

