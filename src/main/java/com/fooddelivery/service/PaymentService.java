package com.fooddelivery.service;

import com.fooddelivery.dto.CreatePaymentRequest;
import com.fooddelivery.dto.PaymentResponse;
import com.fooddelivery.dto.RefundRequest;
import com.fooddelivery.dto.VerifyPaymentRequest;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Payment;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.currency:INR}")
    private String currency;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Transactional
    public PaymentResponse createPaymentOrder(CreatePaymentRequest request, Long userId) {
        // Check if payment already exists for this order
        Payment existingPayment = paymentRepository.findByOrderId(request.getOrderId())
                .orElse(null);

        if (existingPayment != null && existingPayment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment already completed for this order");
        }

        // Get order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        // Validate order belongs to customer
        if (!order.getCustomer().getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to the authenticated user");
        }

        // Check if order is cancelled
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot create payment for cancelled order");
        }

        // Check if payment already exists
        Payment payment;
        if (existingPayment != null) {
            payment = existingPayment;
        } else {
            payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(order.getTotalAmount());
            payment.setMethod(request.getPaymentMethod());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);
        }

        try {
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            // Amount in paise (multiply by 100)
            long amountInPaise = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "order_" + order.getId());
            orderRequest.put("notes", new JSONObject()
                    .put("orderId", order.getId().toString())
                    .put("paymentId", payment.getId().toString()));

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            
            // Store Razorpay order ID in transaction ID temporarily
            payment.setTransactionId(razorpayOrder.get("id"));

            // Convert to response
            PaymentResponse response = convertToPaymentResponse(payment);
            response.setRazorpayOrderId(razorpayOrder.get("id"));
            response.setRazorpayKey(razorpayKeyId);

            return response;

        } catch (com.razorpay.RazorpayException e) {
            throw new BadRequestException("Failed to create Razorpay order: " + e.getMessage() + ". Please check Razorpay configuration.");
        } catch (Exception e) {
            throw new BadRequestException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request, Long userId) {
        // Get payment
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + request.getOrderId()));

        // Validate order belongs to customer
        if (!payment.getOrder().getCustomer().getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to the authenticated user");
        }

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment already verified");
        }

        try {
            // Verify signature
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValidSignature = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (!isValidSignature) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new BadRequestException("Invalid payment signature");
            }

            // Update payment status
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId(request.getRazorpayPaymentId());
            payment = paymentRepository.save(payment);

            // Update order payment status
            Order order = payment.getOrder();
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderRepository.save(order);

            return convertToPaymentResponse(payment);

        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse processRefund(RefundRequest request, Long userId) {
        // Get payment
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + request.getOrderId()));

        // Validate order belongs to customer (or admin/restaurant - can be enhanced)
        if (!payment.getOrder().getCustomer().getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to the authenticated user");
        }

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Can only refund successful payments");
        }

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment already refunded");
        }

        // Validate refund amount
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot exceed payment amount");
        }

        try {
            // Create Razorpay refund
            JSONObject refundRequest = new JSONObject();
            long amountInPaise = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            
            refundRequest.put("amount", amountInPaise);
            refundRequest.put("speed", "normal");
            
            if (request.getReason() != null && !request.getReason().isEmpty()) {
                refundRequest.put("notes", new JSONObject()
                        .put("reason", request.getReason()));
            }

            Refund refund = razorpayClient.payments.refund(payment.getTransactionId(), refundRequest);

            // Update payment status
            if (request.getAmount().compareTo(payment.getAmount()) == 0) {
                // Full refund
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                // Partial refund - keep as SUCCESS but log refund
                // In a real system, you might want to track partial refunds separately
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            }

            payment.setTransactionId(refund.get("id"));
            payment = paymentRepository.save(payment);

            // Update order payment status
            Order order = payment.getOrder();
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
            orderRepository.save(order);

            return convertToPaymentResponse(payment);

        } catch (Exception e) {
            throw new BadRequestException("Refund failed: " + e.getMessage());
        }
    }

    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        
        // Validate order belongs to customer
        if (!payment.getOrder().getCustomer().getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to the authenticated user");
        }
        
        return convertToPaymentResponse(payment);
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setAmount(payment.getAmount());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentDate(payment.getPaymentDate());
        
        // If transaction ID is a Razorpay order ID (contains "order_"), set it
        if (payment.getTransactionId() != null && payment.getTransactionId().startsWith("order_")) {
            response.setRazorpayOrderId(payment.getTransactionId());
        }
        
        response.setRazorpayKey(razorpayKeyId);
        return response;
    }
}

