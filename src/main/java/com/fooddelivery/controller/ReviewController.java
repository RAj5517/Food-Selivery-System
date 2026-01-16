package com.fooddelivery.controller;

import com.fooddelivery.dto.RestaurantReviewsResponse;
import com.fooddelivery.dto.ReviewResponse;
import com.fooddelivery.dto.SubmitReviewRequest;
import com.fooddelivery.service.ReviewService;
import com.fooddelivery.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping("/submit")
    public ResponseEntity<ReviewResponse> submitReview(
            Authentication authentication,
            @Valid @RequestBody SubmitReviewRequest request) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        ReviewResponse response = reviewService.submitReview(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<RestaurantReviewsResponse> getRestaurantReviews(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        RestaurantReviewsResponse response = reviewService.getRestaurantReviews(restaurantId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> reviews = reviewService.getCustomerReviews(userId, pageable);
        return ResponseEntity.ok(reviews);
    }
}

