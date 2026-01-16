# Food Delivery System - Complete Project Plan

## Overview
A comprehensive Spring Boot-based Food Delivery System with complete backend implementation covering all major features from authentication to analytics.

---

## PHASE 1: PROJECT SETUP (Day 1-2)

### Step 1: Initialize Project
- Create Spring Boot project with dependencies:
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - PostgreSQL Driver
  - Validation
  - JWT (io.jsonwebtoken)
  - Spring Mail
  - Spring Cache
  - Swagger (SpringDoc OpenAPI)
- Set up GitHub repository structure
- Create proper folder structure:
  ```
  src/main/java/com/fooddelivery/
    ├── controller/
    ├── service/
    ├── repository/
    ├── model/
    ├── dto/
    ├── config/
    ├── exception/
    ├── util/
    └── FoodDeliveryApplication.java
  ```

### Step 2: Configuration Setup
- Create `application.properties` with environment variables
- Configure database connection (PostgreSQL)
- Set up JWT secret and expiration
- Configure email SMTP settings
- Configure file upload settings
- Configure caching (Redis/Caffeine)

---

## PHASE 2: DATABASE DESIGN (Day 3-4)

### Step 3: Design Database Schema
Create entities in this order:

1. **User** (id, email, password, phone, role, isActive, createdAt, updatedAt)
2. **Customer** (id, user_id, name, profile_image, wallet_balance)
3. **CustomerAddress** (id, customer_id, address_type, street, city, pincode, lat, long, is_default)
4. **Restaurant** (id, user_id, name, cuisine, address, lat, long, is_open, avg_prep_time, rating, is_approved)
5. **Category** (id, name, description)
6. **MenuItem** (id, restaurant_id, category_id, name, price, image, is_veg, is_available, description)
7. **DeliveryPartner** (id, user_id, name, vehicle_type, is_available, current_lat, current_long)
8. **Cart** (id, customer_id, restaurant_id, created_at)
9. **CartItem** (id, cart_id, menu_item_id, quantity, price)
10. **Order** (id, customer_id, restaurant_id, delivery_partner_id, address_id, status, total_amount, payment_status, order_date)
11. **OrderItem** (id, order_id, menu_item_id, quantity, price)
12. **Payment** (id, order_id, amount, method, status, transaction_id, payment_date)
13. **Review** (id, order_id, customer_id, restaurant_id, rating, comment, created_at)

### Step 4: Define Relationships
- User → Customer/Restaurant/DeliveryPartner (One-to-One)
- Customer → Addresses (One-to-Many)
- Customer → Cart (One-to-One)
- Restaurant → MenuItems (One-to-Many)
- Restaurant → Categories (Many-to-Many via MenuItem)
- Cart → CartItems (One-to-Many)
- Order → OrderItems (One-to-Many)
- Order → Customer, Restaurant, DeliveryPartner (Many-to-One)
- Order → Payment (One-to-One)
- Order → Review (One-to-One)

---

## PHASE 3: AUTHENTICATION & SECURITY (Day 5-7)

### Step 5: JWT Implementation
- Create `JwtUtil` class (generate token, validate token, extract username)
- Create `JwtAuthenticationFilter` (intercept requests, validate JWT)
- Create `UserDetailsService` implementation (load user from database)

### Step 6: Security Configuration
- Create `SecurityConfig` class
- Define security filter chain
- Configure role-based access control (CUSTOMER, RESTAURANT, DELIVERY, ADMIN)
- Set up password encoder (BCrypt)
- Define public endpoints (/api/auth/, /swagger-ui/, /v3/api-docs)

### Step 7: Authentication APIs
**Endpoints:**
- `POST /api/auth/register/customer` - Customer registration
- `POST /api/auth/register/restaurant` - Restaurant registration
- `POST /api/auth/register/delivery` - Delivery partner registration
- `POST /api/auth/login` - Login (all roles)
- `POST /api/auth/refresh-token` - Refresh JWT token

### Step 8: Create DTOs
- `LoginRequest`, `RegisterCustomerRequest`, `RegisterRestaurantRequest`, `RegisterDeliveryRequest`
- `AuthResponse` (token, userId, email, role, expiresIn)

### Step 9: Global Exception Handling
- Create custom exceptions:
  - `ResourceNotFoundException`
  - `UnauthorizedException`
  - `BadRequestException`
  - `ConflictException`
- Create `@ControllerAdvice` class
- Return proper error response structure (status, message, timestamp, path)

### Step 10: Input Validation
- Add Jakarta Validation annotations (`@NotNull`, `@Email`, `@Size`, `@Min`, `@Max`, `@Pattern`)
- Validate all request DTOs
- Create custom validators if needed

---

## PHASE 4: CORE DOMAIN - RESTAURANTS & MENU (Day 8-10)

### Step 11: Restaurant Repository
- Create query to find restaurants by cuisine, city, rating
- Create query to find nearby restaurants (distance calculation using lat/long - Haversine formula)
- Implement pagination and sorting

### Step 12: Restaurant Service Layer
- Register restaurant (with document upload)
- Update restaurant profile
- Toggle open/closed status
- Get restaurant by ID
- List all restaurants with pagination, sorting, filtering

### Step 13: Restaurant Controller
**Endpoints:**
- `POST /api/restaurant/register` - Create
- `PUT /api/restaurant/profile` - Update
- `GET /api/restaurant/{id}` - Fetch by ID
- `DELETE /api/restaurant/{id}` - Delete (soft delete)
- `GET /api/restaurants?page=0&size=10&sort=rating,desc&cuisine=Italian&city=Mumbai` - List with pagination

### Step 14: Menu Management
- `POST /api/restaurant/menu/items` - Add menu item
- `PUT /api/restaurant/menu/items/{id}` - Update menu item
- `DELETE /api/restaurant/menu/items/{id}` - Delete menu item
- `GET /api/restaurant/{id}/menu?category=&isVeg=&page=0&size=10` - List menu with filtering & pagination

### Step 15: File Upload
- `POST /api/restaurant/upload-image` - Upload restaurant image
- `POST /api/restaurant/menu/items/{id}/upload-image` - Upload menu item image
- Store files locally or use cloud storage (S3)
- Return file URL in response

---

## PHASE 5: CORE DOMAIN - CUSTOMER & CART (Day 11-13)

### Step 16: Customer APIs
- `GET /api/customer/profile` - Get profile
- `PUT /api/customer/profile` - Update profile
- `POST /api/customer/addresses` - Add delivery address
- `GET /api/customer/addresses` - List addresses
- `PUT /api/customer/addresses/{id}/set-default` - Set default address
- `DELETE /api/customer/addresses/{id}` - Delete address

### Step 17: Cart Management
- `POST /api/cart/add` - Add item to cart
- `PUT /api/cart/items/{id}` - Update quantity
- `DELETE /api/cart/items/{id}` - Remove item
- `GET /api/cart` - Get current cart
- `DELETE /api/cart/clear` - Clear cart

---

## PHASE 6: ORDER MANAGEMENT (Day 14-16)

### Step 18: Order Repository
- Query orders by status, date range, customer, restaurant
- Calculate total revenue by restaurant/date
- Find pending orders for delivery assignment

### Step 19: Order Service (State Machine)
**Order Status Flow:**
```
PENDING → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED
         ↓
      CANCELLED
```

### Step 20: Order APIs
- `POST /api/orders/place` - Create order from cart
- `GET /api/orders/{id}` - Fetch order by ID
- `GET /api/orders?status=&page=0&size=10` - List orders with pagination & filtering
- `PUT /api/orders/{id}/cancel` - Cancel order
- `PUT /api/restaurant/orders/{id}/confirm` - Restaurant confirms
- `PUT /api/restaurant/orders/{id}/ready` - Mark preparing
- `PUT /api/delivery/orders/{id}/pickup` - Delivery picks up
- `PUT /api/delivery/orders/{id}/deliver` - Mark delivered

---

## PHASE 7: PAYMENT INTEGRATION (Day 17-18)

### Step 21: Payment Gateway Integration
- Integrate Razorpay/Stripe
- `POST /api/payments/create-order` - Create payment order
- `POST /api/payments/verify` - Verify payment signature
- `POST /api/payments/refund/{orderId}` - Process refund
- Store payment details in database
- Update order payment_status on success

---

## PHASE 8: DELIVERY PARTNER MODULE (Day 19-20)

### Step 22: Delivery Partner APIs
- `GET /api/delivery/profile` - Get profile
- `PUT /api/delivery/toggle-availability` - Go online/offline
- `GET /api/delivery/orders/available` - Get available orders nearby
- `POST /api/delivery/orders/{id}/accept` - Accept delivery
- `PUT /api/delivery/orders/{id}/update-location` - Update current location (lat, long)
- `GET /api/delivery/earnings?startDate=&endDate=` - View earnings

---

## PHASE 9: CACHING (Day 21)

### Step 23: Implement Caching
- Configure Redis or Caffeine cache
- Cache frequently accessed data:
  - Restaurant details (`@Cacheable`)
  - Menu items by restaurant
  - Customer addresses
- Implement cache eviction on updates (`@CacheEvict`)
- Add cache statistics endpoint

---

## PHASE 10: EMAIL NOTIFICATIONS (Day 22)

### Step 24: Email Service
- Configure SMTP (Gmail/SendGrid)
- Create email templates (HTML)
- Send emails for:
  - Registration confirmation
  - Order confirmation
  - Order status updates
  - Delivery OTP
  - Payment success/failure
- Make email sending async (`@Async`)

---

## PHASE 11: REVIEWS & RATINGS (Day 23)

### Step 25: Review System
- `POST /api/reviews/submit` - Submit review after delivery
- `GET /api/reviews/restaurant/{id}` - Get restaurant reviews
- Calculate and update restaurant average rating
- Complex query: Calculate average rating per restaurant

---

## PHASE 12: ANALYTICS APIs (Day 24-25)

### Step 26: Analytics Implementation

**Admin Analytics:**
- `GET /api/admin/analytics/overview` - Total orders, revenue, active users
- `GET /api/admin/analytics/orders-trend?period=daily|weekly|monthly` - Order trends
- `GET /api/admin/analytics/revenue?startDate=&endDate=` - Revenue analysis
- `GET /api/admin/analytics/top-restaurants?limit=10` - Top restaurants
- `GET /api/admin/analytics/top-customers?limit=10` - Top customers

**Restaurant Analytics:**
- `GET /api/restaurant/analytics/dashboard` - Orders today, revenue, avg rating
- `GET /api/restaurant/analytics/popular-items` - Most ordered items
- `GET /api/restaurant/analytics/revenue?startDate=&endDate=` - Revenue breakdown
- `GET /api/restaurant/analytics/peak-hours` - Busy hours analysis

**Delivery Analytics:**
- `GET /api/delivery/analytics/earnings?period=` - Earnings breakdown
- `GET /api/delivery/analytics/completed-deliveries` - Total deliveries
- `GET /api/delivery/analytics/average-delivery-time` - Performance metrics

Use complex aggregation queries with `GROUP BY`, `AVG`, `SUM`, `COUNT`

---

## PHASE 13: RATE LIMITING (Day 26)

### Step 27: Implement Rate Limiting
- Use Bucket4j or custom Redis-based rate limiter
- Apply rate limits:
  - Public APIs: 20 requests/minute
  - Authenticated APIs: 100 requests/minute
  - Payment APIs: 10 requests/minute
- Return 429 (Too Many Requests) when limit exceeded
- Add rate limit headers (`X-RateLimit-Remaining`, `X-RateLimit-Reset`)

---

## PHASE 14: SWAGGER DOCUMENTATION (Day 27)

### Step 28: API Documentation
- Configure SpringDoc OpenAPI
- Add `@Operation` annotations to all endpoints
- Document request/response examples
- Add API descriptions and tags
- Configure JWT authentication in Swagger UI
- Access at `/swagger-ui.html`

---

## PHASE 15: ADMIN MODULE (Day 28)

### Step 29: Admin APIs
- `GET /api/admin/users?role=&page=0&size=10` - List all users
- `PUT /api/admin/users/{id}/activate` - Activate/deactivate user
- `GET /api/admin/restaurants/pending-approval` - Restaurants awaiting approval
- `PUT /api/admin/restaurants/{id}/approve` - Approve restaurant
- `GET /api/admin/orders/all?status=&page=0&size=10` - View all orders
- `GET /api/admin/analytics/*` - All analytics endpoints

---

## PHASE 16: TESTING & OPTIMIZATION (Day 29-30)

### Step 30: Code Review & Cleanup
- Ensure no business logic in controllers
- Check all validations are in place
- Verify exception handling covers all cases
- Remove hardcoded values
- Use environment variables for secrets
- Add meaningful comments

### Step 31: Performance Optimization
- Add database indexes on frequently queried fields:
  - `email` in User
  - `restaurant_id` in MenuItem
  - `customer_id` in Order
  - `status` in Order
  - `order_date` in Order
- Optimize N+1 query problems (use `@EntityGraph` or `JOIN FETCH`)
- Review and optimize slow queries
- Enable query caching where appropriate
- Set proper connection pool settings

### Step 32: Security Hardening
- Add CORS configuration
- Implement SQL injection prevention (use PreparedStatement via JPA)
- Add XSS protection
- Enable HTTPS (in production)
- Validate all inputs
- Sanitize outputs


## Technology Stack

- **Framework:** Spring Boot 3.x
- **Java Version:** 17+
- **Database:** PostgreSQL
- **Security:** Spring Security + JWT
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Caching:** Redis / Caffeine
- **Email:** Spring Mail (SMTP)
- **Payment:** Razorpay / Stripe
- **Build Tool:** Maven / Gradle
- **Validation:** Jakarta Validation

---

