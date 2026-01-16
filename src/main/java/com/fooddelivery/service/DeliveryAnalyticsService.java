package com.fooddelivery.service;

import com.fooddelivery.dto.DeliveryAnalyticsResponse;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Order;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    private static final BigDecimal DELIVERY_COMMISSION_RATE = new BigDecimal("0.10"); // 10%

    public DeliveryAnalyticsResponse getAnalytics(Long userId, String period) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (period != null ? period.toUpperCase() : "ALL") {
            case "DAILY":
                startDate = endDate.minusDays(1);
                break;
            case "WEEKLY":
                startDate = endDate.minusWeeks(1);
                break;
            case "MONTHLY":
                startDate = endDate.minusMonths(1);
                break;
            default:
                startDate = null;
        }

        List<Order> deliveredOrders;
        if (startDate != null) {
            deliveredOrders = orderRepository.findByDeliveryPartnerIdAndStatusAndOrderDateBetween(
                    deliveryPartner.getId(),
                    Order.OrderStatus.DELIVERED,
                    startDate,
                    endDate
            );
        } else {
            deliveredOrders = orderRepository.findByDeliveryPartnerIdAndStatus(
                    deliveryPartner.getId(),
                    Order.OrderStatus.DELIVERED
            );
        }

        DeliveryAnalyticsResponse response = new DeliveryAnalyticsResponse();

        // Completed deliveries
        response.setCompletedDeliveries((long) deliveredOrders.size());

        // Total earnings
        BigDecimal totalEarnings = deliveredOrders.stream()
                .map(order -> order.getTotalAmount().multiply(DELIVERY_COMMISSION_RATE))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        response.setTotalEarnings(totalEarnings);

        // Average earning per delivery
        BigDecimal averageEarning = deliveredOrders.isEmpty()
                ? BigDecimal.ZERO
                : totalEarnings.divide(BigDecimal.valueOf(deliveredOrders.size()), 2, RoundingMode.HALF_UP);
        response.setAverageEarningPerDelivery(averageEarning);

        // Average delivery time (if data available)
        if (startDate != null) {
            Double avgDeliveryTime = orderRepository.calculateAverageDeliveryTimeMinutes(
                    deliveryPartner.getId(),
                    startDate
            );
            response.setAverageDeliveryTimeMinutes(avgDeliveryTime);
        } else {
            // For "ALL" period, use last 30 days as default
            LocalDateTime defaultStartDate = LocalDateTime.now().minusDays(30);
            Double avgDeliveryTime = orderRepository.calculateAverageDeliveryTimeMinutes(
                    deliveryPartner.getId(),
                    defaultStartDate
            );
            response.setAverageDeliveryTimeMinutes(avgDeliveryTime);
        }

        // Total orders assigned
        List<Order> allAssignedOrders = orderRepository.findByDeliveryPartnerIdAndStatus(
                deliveryPartner.getId(),
                null
        );
        response.setTotalOrdersAssigned((long) allAssignedOrders.size());

        return response;
    }
}

