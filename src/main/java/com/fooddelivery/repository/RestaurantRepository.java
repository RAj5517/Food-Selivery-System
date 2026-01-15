package com.fooddelivery.repository;

import com.fooddelivery.model.Restaurant;
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
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    Optional<Restaurant> findByUserId(Long userId);
    
    // Find restaurants by cuisine
    Page<Restaurant> findByCuisineIgnoreCase(String cuisine, Pageable pageable);
    
    // Find restaurants by city (from address)
    @Query(value = "SELECT r.* FROM restaurant r WHERE LOWER(CAST(r.address AS text)) LIKE LOWER('%' || CAST(:city AS text) || '%')",
           countQuery = "SELECT COUNT(*) FROM restaurant r WHERE LOWER(CAST(r.address AS text)) LIKE LOWER('%' || CAST(:city AS text) || '%')",
           nativeQuery = true)
    Page<Restaurant> findByCity(@Param("city") String city, Pageable pageable);
    
    // Find restaurants by rating
    Page<Restaurant> findByRatingGreaterThanEqual(BigDecimal minRating, Pageable pageable);
    
    // Find restaurants by cuisine and city
    @Query(value = "SELECT r.* FROM restaurant r WHERE LOWER(CAST(r.cuisine AS text)) = LOWER(CAST(:cuisine AS text)) AND LOWER(CAST(r.address AS text)) LIKE LOWER('%' || CAST(:city AS text) || '%')",
           countQuery = "SELECT COUNT(*) FROM restaurant r WHERE LOWER(CAST(r.cuisine AS text)) = LOWER(CAST(:cuisine AS text)) AND LOWER(CAST(r.address AS text)) LIKE LOWER('%' || CAST(:city AS text) || '%')",
           nativeQuery = true)
    Page<Restaurant> findByCuisineAndCity(@Param("cuisine") String cuisine, @Param("city") String city, Pageable pageable);
    
    // Find nearby restaurants using Haversine formula (distance in km)
    @Query(value = "SELECT r.*, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(r.lat)) * " +
            "cos(radians(r.longitude) - radians(:longitude)) + " +
            "sin(radians(:lat)) * sin(radians(r.lat)))) AS distance " +
            "FROM restaurant r " +
            "WHERE r.is_open = true AND r.is_approved = true " +
            "HAVING distance <= :radius " +
            "ORDER BY distance ASC",
            nativeQuery = true)
    List<Restaurant> findNearbyRestaurants(@Param("lat") Double lat, 
                                          @Param("longitude") Double longitude, 
                                          @Param("radius") Double radius);
    
    // Find approved restaurants
    Page<Restaurant> findByIsApprovedTrue(Pageable pageable);
    
    // Find open restaurants
    Page<Restaurant> findByIsOpenTrueAndIsApprovedTrue(Pageable pageable);
    
    // Find restaurants by cuisine, city, and minimum rating
    @Query(value = "SELECT r.* FROM restaurant r WHERE " +
           "(:cuisine IS NULL OR LOWER(CAST(r.cuisine AS text)) = LOWER(CAST(:cuisine AS text))) AND " +
           "(:city IS NULL OR LOWER(CAST(r.address AS text)) LIKE LOWER('%' || CAST(:city AS text) || '%')) AND " +
           "(:minRating IS NULL OR r.rating >= :minRating) AND " +
           "r.is_approved = true",
           countQuery = "SELECT COUNT(*) FROM restaurant r WHERE " +
           "(:cuisine IS NULL OR LOWER(CAST(r.cuisine AS text)) = LOWER(CAST(:cuisine AS text))) AND " +
           "(:city IS NULL OR LOWER(CAST(r.address AS text)) LIKE LOWER('%' || CAST(:city AS text) || '%')) AND " +
           "(:minRating IS NULL OR r.rating >= :minRating) AND " +
           "r.is_approved = true",
           nativeQuery = true)
    Page<Restaurant> findRestaurantsWithFilters(@Param("cuisine") String cuisine,
                                                 @Param("city") String city,
                                                 @Param("minRating") BigDecimal minRating,
                                                 Pageable pageable);
}
