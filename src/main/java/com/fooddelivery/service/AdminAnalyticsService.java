package com.fooddelivery.service;

import com.fooddelivery.dto.AdminAnalyticsOverviewResponse;
import com.fooddelivery.dto.OrderTrendResponse;
import com.fooddelivery.dto.TopCustomerResponse;
import com.fooddelivery.dto.TopRestaurantResponse;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public AdminAnalyticsOverviewResponse getOverview() {
        AdminAnalyticsOverviewResponse response = new AdminAnalyticsOverviewResponse();

        // Total orders
        response.setTotalOrders(orderRepository.count());

        // Total revenue (sum of all paid orders)
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenueByDateRange(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.now()
        );
        response.setTotalRevenue(totalRevenue);

        // Active users (users with isActive = true)
        Long activeUsers = userRepository.count();
        response.setActiveUsers(activeUsers);

        // Active restaurants (approved restaurants)
        Long activeRestaurants = restaurantRepository.findAll().stream()
                .filter(r -> r.getIsApproved() != null && r.getIsApproved())
                .count();
        response.setActiveRestaurants(activeRestaurants);

        // Total customers
        Long totalCustomers = customerRepository.count();
        response.setTotalCustomers(totalCustomers);

        // Total delivery partners
        Long totalDeliveryPartners = deliveryPartnerRepository.count();
        response.setTotalDeliveryPartners(totalDeliveryPartners);

        // Pending orders
        Long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        response.setPendingOrders(pendingOrders);

        // Completed orders (DELIVERED)
        Long completedOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        response.setCompletedOrders(completedOrders);

        return response;
    }

    public List<OrderTrendResponse> getOrdersTrend(String period) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (period.toUpperCase()) {
            case "DAILY":
                startDate = endDate.minusDays(30); // Last 30 days
                break;
            case "WEEKLY":
                startDate = endDate.minusWeeks(12); // Last 12 weeks
                break;
            case "MONTHLY":
                startDate = endDate.minusMonths(12); // Last 12 months
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        List<Object[]> results = orderRepository.countOrdersByDate(startDate, endDate);

        return results.stream().map(row -> {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Long count = ((Number) row[1]).longValue();
            BigDecimal revenue = ((BigDecimal) row[2]);
            
            return new OrderTrendResponse(date, count, revenue);
        }).collect(Collectors.toList());
    }

    public BigDecimal getRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) {
            return orderRepository.calculateTotalRevenueByDateRange(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now()
            );
        } else if (startDate == null) {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        } else if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        return orderRepository.calculateTotalRevenueByDateRange(startDate, endDate);
    }

    public List<TopRestaurantResponse> getTopRestaurants(int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1); // Last month
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> results = orderRepository.findTopRestaurantsByOrders(
                startDate, 
                endDate, 
                PageRequest.of(0, limit)
        );

        return results.stream().map(row -> {
            Long restaurantId = ((Number) row[0]).longValue();
            String restaurantName = (String) row[1];
            Long orderCount = ((Number) row[2]).longValue();
            BigDecimal totalRevenue = ((BigDecimal) row[3]);
            Double avgRating = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            
            return new TopRestaurantResponse(
                    restaurantId,
                    restaurantName,
                    orderCount,
                    totalRevenue,
                    BigDecimal.valueOf(avgRating).setScale(2, java.math.RoundingMode.HALF_UP)
            );
        }).collect(Collectors.toList());
    }

    public List<TopCustomerResponse> getTopCustomers(int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1); // Last month
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> results = orderRepository.findTopCustomersByOrders(
                startDate,
                endDate,
                PageRequest.of(0, limit)
        );

        return results.stream().map(row -> {
            Long customerId = ((Number) row[0]).longValue();
            String customerName = (String) row[1];
            Long orderCount = ((Number) row[2]).longValue();
            BigDecimal totalSpent = ((BigDecimal) row[3]);
            
            return new TopCustomerResponse(customerId, customerName, orderCount, totalSpent);
        }).collect(Collectors.toList());
    }
}

