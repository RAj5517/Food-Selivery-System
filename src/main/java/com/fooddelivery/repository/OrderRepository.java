package com.fooddelivery.repository;

import com.fooddelivery.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by customer
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    // Find orders by restaurant
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    // Find orders by delivery partner
    Page<Order> findByDeliveryPartnerId(Long deliveryPartnerId, Pageable pageable);
    
    // Find orders by status
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by customer and status
    Page<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status, Pageable pageable);
    
    // Find orders by restaurant and status
    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status, Pageable pageable);
    
    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);
    
    // Find orders by customer and date range
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByCustomerIdAndOrderDateBetween(@Param("customerId") Long customerId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     Pageable pageable);
    
    // Find orders by restaurant and date range
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByRestaurantIdAndOrderDateBetween(@Param("restaurantId") Long restaurantId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      Pageable pageable);
    
    // Calculate total revenue by restaurant and date range
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderDate BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID'")
    BigDecimal calculateRevenueByRestaurantAndDateRange(@Param("restaurantId") Long restaurantId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);
    
    // Calculate total revenue by date range
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID'")
    BigDecimal calculateTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    // Find pending orders for delivery assignment (no delivery partner assigned)
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.deliveryPartner IS NULL")
    List<Order> findPendingOrdersForDelivery(Pageable pageable);
    
    // Count orders by status
    Long countByStatus(Order.OrderStatus status);
    
    // Count orders by restaurant and status
    Long countByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status);
    
    // Count orders by customer
    Long countByCustomerId(Long customerId);
}

