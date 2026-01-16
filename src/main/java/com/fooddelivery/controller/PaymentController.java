package com.fooddelivery.controller;

import com.fooddelivery.dto.CreatePaymentRequest;
import com.fooddelivery.dto.PaymentResponse;
import com.fooddelivery.dto.RefundRequest;
import com.fooddelivery.dto.VerifyPaymentRequest;
import com.fooddelivery.service.PaymentService;
import com.fooddelivery.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createPaymentOrder(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        PaymentResponse response = paymentService.createPaymentOrder(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        PaymentResponse response = paymentService.verifyPayment(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> processRefund(
            Authentication authentication,
            @Valid @RequestBody RefundRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        // Note: In production, only admins/restaurants should be able to refund
        PaymentResponse response = paymentService.processRefund(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, userId);
        return ResponseEntity.ok(response);
    }
}


