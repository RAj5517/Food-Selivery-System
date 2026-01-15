package com.fooddelivery.dto;

import com.fooddelivery.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private AddressResponse address;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private Order.PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
}

