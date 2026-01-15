package com.fooddelivery.service;

import com.fooddelivery.dto.*;
import com.fooddelivery.exception.ConflictException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.model.*;
import com.fooddelivery.repository.*;
import com.fooddelivery.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(User.Role.CUSTOMER);
        user.setIsActive(true);
        user = userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(request.getName());
        customer.setWalletBalance(java.math.BigDecimal.ZERO);
        customer = customerRepository.save(customer);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setExpiresIn(jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis());

        return response;
    }

    @Transactional
    public AuthResponse registerRestaurant(RegisterRestaurantRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(User.Role.RESTAURANT);
        user.setIsActive(true);
        user = userRepository.save(user);

        Restaurant restaurant = new Restaurant();
        restaurant.setUser(user);
        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setLat(request.getLat());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setIsOpen(false);
        restaurant.setIsApproved(false);
        restaurant.setRating(java.math.BigDecimal.ZERO);
        restaurant = restaurantRepository.save(restaurant);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setExpiresIn(jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis());

        return response;
    }

    @Transactional
    public AuthResponse registerDelivery(RegisterDeliveryRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(User.Role.DELIVERY);
        user.setIsActive(true);
        user = userRepository.save(user);

        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setUser(user);
        deliveryPartner.setName(request.getName());
        deliveryPartner.setVehicleType(request.getVehicleType());
        deliveryPartner.setIsAvailable(false);
        deliveryPartner = deliveryPartnerRepository.save(deliveryPartner);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setExpiresIn(jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis());

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            if (!user.getIsActive()) {
                throw new UnauthorizedException("Account is deactivated");
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole().name());
            response.setExpiresIn(jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis());

            return response;
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}

