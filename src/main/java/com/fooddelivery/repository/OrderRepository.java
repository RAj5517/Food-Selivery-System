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
    
    // Find orders by delivery partner and status
    List<Order> findByDeliveryPartnerIdAndStatus(Long deliveryPartnerId, Order.OrderStatus status);
    
    // Find orders by delivery partner, status, and date range
    @Query("SELECT o FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId AND o.status = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByDeliveryPartnerIdAndStatusAndOrderDateBetween(
            @Param("deliveryPartnerId") Long deliveryPartnerId,
            @Param("status") Order.OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find orders by delivery partner, status, and after start date
    @Query("SELECT o FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId AND o.status = :status AND o.orderDate >= :startDate")
    List<Order> findByDeliveryPartnerIdAndStatusAndOrderDateAfter(
            @Param("deliveryPartnerId") Long deliveryPartnerId,
            @Param("status") Order.OrderStatus status,
            @Param("startDate") LocalDateTime startDate);
    
    // Analytics methods
    
    // Count orders by date (for trends)
    @Query("SELECT FUNCTION('DATE', o.orderDate) as orderDate, COUNT(o.id) as orderCount, COALESCE(SUM(o.totalAmount), 0) as revenue " +
           "FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID' " +
           "GROUP BY FUNCTION('DATE', o.orderDate) ORDER BY orderDate ASC")
    List<Object[]> countOrdersByDate(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Top restaurants by orders
    @Query("SELECT r.id, r.name, COUNT(o.id) as orderCount, COALESCE(SUM(o.totalAmount), 0) as totalRevenue, AVG(r.rating) as avgRating " +
           "FROM Order o JOIN o.restaurant r WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID' " +
           "GROUP BY r.id, r.name ORDER BY orderCount DESC")
    List<Object[]> findTopRestaurantsByOrders(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);
    
    // Top customers by orders
    @Query("SELECT c.id, c.name, COUNT(o.id) as orderCount, COALESCE(SUM(o.totalAmount), 0) as totalSpent " +
           "FROM Order o JOIN o.customer c WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.paymentStatus = 'PAID' " +
           "GROUP BY c.id, c.name ORDER BY orderCount DESC")
    List<Object[]> findTopCustomersByOrders(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);
    
    // Count orders today by restaurant
    @Query("SELECT COUNT(o.id) FROM Order o WHERE o.restaurant.id = :restaurantId " +
           "AND FUNCTION('DATE', o.orderDate) = CURRENT_DATE")
    Long countOrdersTodayByRestaurant(@Param("restaurantId") Long restaurantId);
    
    // Calculate revenue today by restaurant
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.restaurant.id = :restaurantId " +
           "AND FUNCTION('DATE', o.orderDate) = CURRENT_DATE AND o.paymentStatus = 'PAID'")
    BigDecimal calculateRevenueTodayByRestaurant(@Param("restaurantId") Long restaurantId);
    
    // Popular items by restaurant
    @Query("SELECT mi.id, mi.name, COUNT(DISTINCT o.id) as orderCount, SUM(oi.quantity) as totalQuantity " +
           "FROM Order o JOIN o.orderItems oi JOIN oi.menuItem mi " +
           "WHERE o.restaurant.id = :restaurantId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY mi.id, mi.name ORDER BY orderCount DESC, totalQuantity DESC")
    List<Object[]> findPopularItemsByRestaurant(@Param("restaurantId") Long restaurantId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 Pageable pageable);
    
    // Peak hours by restaurant
    @Query("SELECT EXTRACT(HOUR FROM o.orderDate) as hour, COUNT(o.id) as orderCount " +
           "FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY EXTRACT(HOUR FROM o.orderDate) ORDER BY orderCount DESC")
    List<Object[]> findPeakHoursByRestaurant(@Param("restaurantId") Long restaurantId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    // Calculate average delivery time in minutes
    // Using native query because HQL doesn't support EXTRACT(EPOCH FROM (timestamp - timestamp))
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (delivered_date - order_date)) / 60) " +
           "FROM \"order\" WHERE delivery_partner_id = :deliveryPartnerId " +
           "AND status = 'DELIVERED' AND delivered_date IS NOT NULL AND order_date >= :startDate",
           nativeQuery = true)
    Double calculateAverageDeliveryTimeMinutes(@Param("deliveryPartnerId") Long deliveryPartnerId,
                                                @Param("startDate") LocalDateTime startDate);
}

