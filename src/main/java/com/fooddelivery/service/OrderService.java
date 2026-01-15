package com.fooddelivery.service;

import com.fooddelivery.dto.AddressResponse;
import com.fooddelivery.dto.OrderItemResponse;
import com.fooddelivery.dto.OrderResponse;
import com.fooddelivery.dto.PlaceOrderRequest;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.*;
import com.fooddelivery.repository.CartItemRepository;
import com.fooddelivery.repository.CartRepository;
import com.fooddelivery.repository.CustomerAddressRepository;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.OrderItemRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        CustomerAddress address = addressRepository.findByIdAndCustomerId(request.getAddressId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        Restaurant restaurant = cart.getRestaurant();
        if (restaurant == null) {
            throw new BadRequestException("Cart does not have a restaurant");
        }

        // Calculate total
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setAddress(address);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(cartItem.getMenuItem());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItemRepository.save(orderItem);
        }

        // Clear cart
        cartItemRepository.deleteByCartId(cart.getId());

        return convertToOrderResponse(order);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return convertToOrderResponse(order);
    }

    public Page<OrderResponse> getCustomerOrders(Long userId, Order.OrderStatus status, Pageable pageable) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByCustomerIdAndStatus(customer.getId(), status, pageable);
        } else {
            orders = orderRepository.findByCustomerId(customer.getId(), pageable);
        }

        return orders.map(this::convertToOrderResponse);
    }

    public Page<OrderResponse> getRestaurantOrders(Long userId, Order.OrderStatus status, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByRestaurantIdAndStatus(restaurant.getId(), status, pageable);
        } else {
            orders = orderRepository.findByRestaurantId(restaurant.getId(), pageable);
        }

        return orders.map(this::convertToOrderResponse);
    }

    public Page<OrderResponse> getAvailableOrdersForDelivery(Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(Order.OrderStatus.PREPARING, pageable);
        // Filter out orders that already have a delivery partner
        List<Order> availableOrders = orders.getContent().stream()
                .filter(order -> order.getDeliveryPartner() == null)
                .collect(Collectors.toList());
        
        // Convert to Page
        Page<Order> availableOrdersPage = new org.springframework.data.domain.PageImpl<>(
                availableOrders, pageable, availableOrders.size());
        return availableOrdersPage.map(this::convertToOrderResponse);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("You can only cancel your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrder(Long restaurantUserId, Long orderId) {
        Restaurant restaurant = restaurantRepository.findByUserId(restaurantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BadRequestException("Order does not belong to this restaurant");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be confirmed");
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    @Transactional
    public OrderResponse markOrderReady(Long restaurantUserId, Long orderId) {
        Restaurant restaurant = restaurantRepository.findByUserId(restaurantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BadRequestException("Order does not belong to this restaurant");
        }

        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order must be confirmed before marking as ready");
        }

        order.setStatus(Order.OrderStatus.PREPARING);
        order = orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    @Transactional
    public OrderResponse acceptDelivery(Long deliveryUserId, Long orderId) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByUserId(deliveryUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PREPARING) {
            throw new BadRequestException("Order must be in PREPARING status to accept delivery");
        }

        if (order.getDeliveryPartner() != null) {
            throw new BadRequestException("Order is already assigned to a delivery partner");
        }

        order.setDeliveryPartner(deliveryPartner);
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        order = orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    @Transactional
    public OrderResponse markOrderDelivered(Long deliveryUserId, Long orderId) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByUserId(deliveryUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartner.getId())) {
            throw new BadRequestException("Order is not assigned to this delivery partner");
        }

        if (order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Order must be out for delivery to mark as delivered");
        }

        order.setStatus(Order.OrderStatus.DELIVERED);
        order = orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomer().getId());
        response.setCustomerName(order.getCustomer().getName());
        response.setRestaurantId(order.getRestaurant().getId());
        response.setRestaurantName(order.getRestaurant().getName());
        
        if (order.getDeliveryPartner() != null) {
            response.setDeliveryPartnerId(order.getDeliveryPartner().getId());
            response.setDeliveryPartnerName(order.getDeliveryPartner().getName());
        }
        
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderDate(order.getOrderDate());

        // Convert address
        if (order.getAddress() != null) {
            AddressResponse addressResponse = new AddressResponse();
            addressResponse.setId(order.getAddress().getId());
            addressResponse.setStreet(order.getAddress().getStreet());
            addressResponse.setCity(order.getAddress().getCity());
            addressResponse.setPincode(order.getAddress().getPincode());
            addressResponse.setLat(order.getAddress().getLat());
            addressResponse.setLongitude(order.getAddress().getLongitude());
            addressResponse.setAddressType(order.getAddress().getAddressType());
            addressResponse.setIsDefault(order.getAddress().getIsDefault());
            response.setAddress(addressResponse);
        }

        // Convert order items
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(items);

        return response;
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMenuItemId(orderItem.getMenuItem().getId());
        response.setMenuItemName(orderItem.getMenuItem().getName());
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());
        response.setSubtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return response;
    }
}

