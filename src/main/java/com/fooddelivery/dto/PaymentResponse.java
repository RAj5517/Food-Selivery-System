package com.fooddelivery.dto;

import com.fooddelivery.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private Payment.PaymentMethod method;
    private Payment.PaymentStatus status;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String razorpayOrderId;  // Razorpay order ID for frontend
    private String razorpayKey;      // Razorpay key ID for frontend
}


