package com.fooddelivery.service;

import com.fooddelivery.dto.RestaurantReviewsResponse;
import com.fooddelivery.dto.ReviewResponse;
import com.fooddelivery.dto.SubmitReviewRequest;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Customer;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.Review;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Transactional
    public ReviewResponse submitReview(SubmitReviewRequest request, Long userId) {
        // Get customer
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Get order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        // Validate order belongs to customer
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Order does not belong to the authenticated customer");
        }

        // Validate order is delivered
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Can only review delivered orders");
        }

        // Check if review already exists for this order
        if (reviewRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BadRequestException("Review already exists for this order");
        }

        // Create review
        Review review = new Review();
        review.setOrder(order);
        review.setCustomer(customer);
        review.setRestaurant(order.getRestaurant());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);

        // Update restaurant average rating
        updateRestaurantRating(order.getRestaurant().getId());

        return convertToResponse(review);
    }

    public RestaurantReviewsResponse getRestaurantReviews(Long restaurantId, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // Get reviews with pagination
        Page<Review> reviewsPage = reviewRepository.findByRestaurantId(restaurantId, pageable);
        List<ReviewResponse> reviews = reviewsPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Calculate average rating
        Double avgRating = reviewRepository.calculateAverageRatingByRestaurantId(restaurantId);
        BigDecimal averageRating = avgRating != null 
                ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get total review count
        Long totalReviews = reviewRepository.countByRestaurantId(restaurantId);

        RestaurantReviewsResponse response = new RestaurantReviewsResponse();
        response.setRestaurantId(restaurant.getId());
        response.setRestaurantName(restaurant.getName());
        response.setAverageRating(averageRating);
        response.setTotalReviews(totalReviews);
        response.setReviews(reviews);

        return response;
    }

    public Page<ReviewResponse> getCustomerReviews(Long userId, Pageable pageable) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Page<Review> reviews = reviewRepository.findByCustomerId(customer.getId(), pageable);
        return reviews.map(this::convertToResponse);
    }

    @Transactional
    private void updateRestaurantRating(Long restaurantId) {
        // Calculate average rating
        Double avgRating = reviewRepository.calculateAverageRatingByRestaurantId(restaurantId);
        
        if (avgRating != null) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
            
            BigDecimal rating = BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP);
            restaurant.setRating(rating);
            restaurantRepository.save(restaurant);
        }
    }

    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setOrderId(review.getOrder().getId());
        response.setCustomerId(review.getCustomer().getId());
        response.setCustomerName(review.getCustomer().getName());
        response.setRestaurantId(review.getRestaurant().getId());
        response.setRestaurantName(review.getRestaurant().getName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}

