# Food Delivery System - Phase-by-Phase Implementation Details

## Table of Contents
1. [Phase 1-2: Project Setup & Database Design](#phase-1-2-project-setup--database-design)
2. [Phase 3: Authentication & Security](#phase-3-authentication--security)
3. [Phase 4: Restaurant & Menu Management](#phase-4-restaurant--menu-management)
4. [Phase 5: Customer & Cart Management](#phase-5-customer--cart-management)
5. [Phase 6: Order Management](#phase-6-order-management)
6. [Phase 7: Payment Integration](#phase-7-payment-integration)
7. [Phase 8: Delivery Partner Module](#phase-8-delivery-partner-module)
8. [Phase 9: Caching](#phase-9-caching)
9. [Phase 10: Email Notifications](#phase-10-email-notifications)
10. [Phase 11: Reviews & Ratings](#phase-11-reviews--ratings)
11. [Phase 12: Analytics APIs](#phase-12-analytics-apis)
12. [Phase 13: Rate Limiting](#phase-13-rate-limiting)
13. [Phase 14: Swagger Documentation](#phase-14-swagger-documentation)
14. [Phase 15: Admin Module](#phase-15-admin-module)

---

## Phase 1-2: Project Setup & Database Design

### What Was Done
- Initialized Spring Boot 3.2.0 project with Maven
- Configured dependencies (Spring Web, Security, Data JPA, PostgreSQL, JWT, Mail, Cache, Swagger)
- Created folder structure (controller, service, repository, model, dto, config, exception, util, security)
- Designed and implemented 13 database entities with relationships

### Key Entities Created
1. **User**: Central authentication entity (email, password, role, isActive)
2. **Customer**: Customer profile linked to User (one-to-one)
3. **CustomerAddress**: Multiple delivery addresses per customer (one-to-many)
4. **Restaurant**: Restaurant information with approval status (linked to User)
5. **Category**: Food categories
6. **MenuItem**: Restaurant menu items (linked to Restaurant and Category)
7. **DeliveryPartner**: Delivery partner profile with location (linked to User)
8. **Cart**: Shopping cart for customers (one-to-one with Customer)
9. **CartItem**: Items in shopping cart (one-to-many with Cart)
10. **Order**: Order entity with status tracking (linked to Customer, Restaurant, DeliveryPartner)
11. **OrderItem**: Items in an order (one-to-many with Order)
12. **Payment**: Payment information (one-to-one with Order)
13. **Review**: Customer reviews (one-to-one with Order)

### Why This Approach
- **Layered Architecture**: Clear separation of concerns for maintainability
- **JPA Entities**: Automatic database schema generation from entities
- **DTO Pattern**: Separated API contracts from internal entities
- **PostgreSQL**: Production-ready relational database

### Key Decisions
- Used `@OneToOne` for User → Customer/Restaurant/DeliveryPartner (one account per role)
- Used `@OneToMany` for Customer → Addresses, Restaurant → MenuItems, Order → OrderItems
- Added `isApproved` flag to Restaurant for admin approval workflow
- Added `deliveredDate` to Order for delivery time analytics

---

## Phase 3: Authentication & Security

### What Was Done
- Implemented JWT (JSON Web Token) authentication
- Configured Spring Security with JWT filter
- Created authentication endpoints (register customer/restaurant/delivery, login)
- Implemented role-based access control (CUSTOMER, RESTAURANT, DELIVERY, ADMIN)
- Added password hashing with BCrypt
- Created global exception handler for consistent error responses
- Added input validation using Jakarta Validation

### Key Components
- **JwtUtil**: Generates and validates JWT tokens with email and role claims
- **JwtAuthenticationFilter**: Intercepts requests, validates tokens, sets security context
- **SecurityConfig**: Configures security filter chain, public/protected endpoints
- **AuthService**: Handles registration and login logic
- **CustomUserDetailsService**: Loads user details for Spring Security
- **GlobalExceptionHandler**: Centralized exception handling with proper HTTP status codes

### Why JWT
- **Stateless**: No server-side session storage needed
- **Scalable**: Works across multiple servers
- **Secure**: Signed tokens prevent tampering
- **Self-contained**: Token contains user identity and role

### Security Features Implemented
- Password encryption using BCrypt (strength 10)
- Token expiration (configurable, default 7 days)
- Role-based authorization (`@PreAuthorize` annotations)
- Public endpoints for registration/login
- Protected endpoints require valid JWT token
- Custom exceptions for unauthorized access (401, 403)

---

## Phase 4: Restaurant & Menu Management

### What Was Done
- Implemented restaurant registration and profile management
- Created menu item CRUD operations for restaurants
- Added public restaurant browsing endpoints (no authentication required)
- Implemented restaurant filtering (cuisine, city, rating)
- Added nearby restaurant search using Haversine formula (distance calculation)
- Implemented file upload for restaurant and menu item images
- Added category management for menu items

### Key Features
- **Restaurant Registration**: Creates User and Restaurant entities, sets `isApproved=false` (requires admin approval)
- **Menu Management**: Restaurants can add, update, delete menu items
- **Public Browsing**: Customers can view restaurants and menus without authentication
- **Location-Based Search**: Finds restaurants within radius using latitude/longitude
- **File Upload**: Stores restaurant and menu item images in `uploads/` directory

### Technical Implementation
- **Haversine Formula**: Calculates distance between two coordinates for nearby search
- **JPA Queries**: Custom queries in RestaurantRepository for filtering and location search
- **Pagination**: All list endpoints support pagination (page, size parameters)
- **Sorting**: Sortable by rating, name, etc.
- **Cache Preparation**: Structure ready for caching (implemented in Phase 9)

---

## Phase 5: Customer & Cart Management

### What Was Done
- Implemented customer profile management (view, update)
- Created address management (add, list, update, delete, set default)
- Built shopping cart functionality (add items, update quantities, remove items, clear cart)
- Added cart validation (same restaurant constraint, item availability)
- Implemented cart-to-order conversion logic

### Key Features
- **Profile Management**: Customers can update name and profile image
- **Multiple Addresses**: Customers can add multiple delivery addresses with default selection
- **Shopping Cart**: Persistent cart stored in database (one cart per customer)
- **Cart Validation**: Ensures all items are from same restaurant
- **Cart Totals**: Automatic calculation of cart total from items

### Business Rules
- One default address per customer (setting new default unsets previous)
- Cart can only contain items from one restaurant at a time
- Cart item prices stored at time of addition (price changes don't affect cart)
- Cart automatically cleared after order placement

---

## Phase 6: Order Management

### What Was Done
- Implemented order placement from shopping cart
- Created order status state machine (PENDING → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED)
- Added order management for customers (view, list, cancel)
- Implemented restaurant order management (confirm, update status)
- Added delivery partner order management (view available, accept, mark delivered)
- Integrated email notifications for order status changes

### Order Status Flow
```
PENDING → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED
         ↓
      CANCELLED
```

### Key Features
- **Order Creation**: Converts cart items to order items, clears cart, calculates total
- **Status Transitions**: Validated state machine ensures valid status changes
- **Order Tracking**: Customers can view order status in real-time
- **Restaurant Updates**: Restaurants confirm orders and update preparation status
- **Delivery Assignment**: Delivery partners can accept available orders
- **Cancellation**: Customers can cancel orders before confirmation

### Business Logic
- Orders can only be cancelled in PENDING status
- Restaurant confirms order, then updates to PREPARING
- Restaurant marks order ready, then delivery partner picks up
- Delivery partner marks order delivered (sets `deliveredDate`)
- Email notifications sent at each status change

---

## Phase 7: Payment Integration

### What Was Done
- Integrated Razorpay payment gateway
- Implemented payment order creation
- Added payment verification with signature validation
- Created refund processing
- Added payment history tracking
- Linked payments to orders (one-to-one relationship)

### Key Components
- **RazorpayConfig**: Configures Razorpay client with API keys
- **PaymentService**: Handles payment operations (create, verify, refund)
- **Payment Entity**: Stores payment details (amount, method, status, transaction ID)
- **Payment Controller**: Exposes payment endpoints (rate limited to 10/min)

### Payment Flow
1. **Create Payment Order**: Client calls `/api/payments/create-order`, receives Razorpay order ID
2. **Client Pays**: Frontend redirects to Razorpay checkout
3. **Verify Payment**: Client calls `/api/payments/verify` with Razorpay signature
4. **Update Order**: Payment status updated to PAID, order payment status updated
5. **Refund**: Admin/Restaurant can process refunds through `/api/payments/refund/{orderId}`

### Security
- **Signature Verification**: Validates Razorpay webhook signatures to prevent fraud
- **Transaction ID Storage**: Tracks all payment transactions for audit
- **Rate Limiting**: Payment endpoints limited to 10 requests/minute

---

## Phase 8: Delivery Partner Module

### What Was Done
- Implemented delivery partner profile management
- Created availability toggle (online/offline)
- Added location tracking (latitude/longitude updates)
- Implemented available orders listing (orders ready for pickup)
- Added order acceptance and delivery completion
- Created earnings tracking functionality

### Key Features
- **Profile Management**: Delivery partners can update name and vehicle type
- **Availability Status**: Toggle online/offline to receive order assignments
- **Location Updates**: Update current location for order assignment algorithms
- **Order Assignment**: Delivery partners can accept available orders
- **Delivery Completion**: Mark orders as delivered with timestamp
- **Earnings Tracking**: View earnings by period (today, week, month)

### Business Logic
- Only online delivery partners see available orders
- Order assignment updates order status to OUT_FOR_DELIVERY
- Delivery completion sets `deliveredDate` and updates status to DELIVERED
- Earnings calculated from delivery fees (future enhancement: commission-based)

---

## Phase 9: Caching

### What Was Done
- Configured Caffeine cache (in-memory caching)
- Implemented caching for restaurant details
- Added caching for menu items by restaurant
- Implemented cache eviction on updates
- Created cache statistics endpoint for monitoring

### Cache Strategy
- **Cache Names**: "restaurants", "menuItems"
- **Cache Size**: Maximum 500 entries per cache
- **Expiration**: 30 minutes after write
- **Eviction**: Automatic on updates (`@CacheEvict` annotation)

### Why Caffeine
- **In-Memory**: Fast access for frequently accessed data
- **No External Dependency**: No need for Redis server
- **Automatic Management**: Handles eviction and size limits
- **Good for Small-Medium Scale**: Sufficient for typical food delivery platform

### Caching Implementation
- `@Cacheable`: Caches restaurant and menu item responses
- `@CacheEvict`: Evicts cache when restaurant or menu item updated
- Cache keys based on entity IDs for efficient lookup
- Cache statistics exposed via `/api/cache/stats` endpoint

---

## Phase 10: Email Notifications

### What Was Done
- Configured Spring Mail with SMTP settings
- Created EmailService for sending emails
- Implemented HTML email templates
- Added email notifications for registration, order status changes
- Made email sending asynchronous with `@Async`

### Email Types Implemented
1. **Registration Confirmation**: Sent when user registers
2. **Order Confirmation**: Sent when order placed
3. **Order Status Updates**: Sent when order status changes (confirmed, preparing, out for delivery, delivered)
4. **Order Cancellation**: Sent when order cancelled

### Technical Details
- **SMTP Configuration**: Configurable via `application.properties`
- **HTML Templates**: Formatted email templates with order details
- **Asynchronous Sending**: `@Async` prevents blocking request thread
- **Error Handling**: Failed emails logged but don't fail the operation

### Why Email Notifications
- **User Engagement**: Keeps users informed about order status
- **Trust Building**: Confirmation emails increase user confidence
- **Reduces Support**: Users get automatic updates, fewer inquiries
- **Professional**: Standard practice for e-commerce platforms

---

## Phase 11: Reviews & Ratings

### What Was Done
- Implemented review submission after order delivery
- Created review retrieval by restaurant
- Added average rating calculation for restaurants
- Implemented rating update on restaurant profile
- Added validation (one review per order, order must be delivered)

### Key Features
- **Review Submission**: Customers can review restaurants after order delivery
- **Rating Calculation**: Automatic average rating calculation from all reviews
- **Restaurant Rating Update**: Restaurant's average rating updated after each review
- **Review Retrieval**: Customers can view all reviews for a restaurant

### Business Rules
- One review per order (prevents duplicate reviews)
- Review only allowed for delivered orders
- Rating automatically updates restaurant's average rating
- Reviews stored with order, customer, restaurant references

### Technical Implementation
- **ReviewRepository**: Custom query to calculate average rating using `AVG()` aggregation
- **ReviewService**: Validates review eligibility, updates restaurant rating
- **Rating Update**: Restaurant entity's `rating` field updated after each review submission

---

## Phase 12: Analytics APIs

### What Was Done
- Implemented Admin analytics (system-wide overview, revenue, trends, top restaurants/customers)
- Created Restaurant analytics (dashboard, popular items, revenue breakdown, peak hours)
- Added Delivery analytics (earnings, completed deliveries, average delivery time)
- Used complex aggregation queries (SUM, COUNT, AVG, GROUP BY)
- Added date range filtering for all analytics

### Admin Analytics Endpoints
- **Overview**: Total orders, revenue, users, restaurants
- **Order Trends**: Daily/weekly/monthly order trends with counts and revenue
- **Revenue Analysis**: Revenue breakdown by date range
- **Top Restaurants**: Most popular restaurants by order count and revenue
- **Top Customers**: Most active customers by order count and spending

### Restaurant Analytics Endpoints
- **Dashboard**: Today's orders, revenue, average rating
- **Popular Items**: Most ordered menu items with quantities
- **Revenue Breakdown**: Revenue by date range
- **Peak Hours**: Busiest hours for orders (hourly analysis)

### Delivery Analytics Endpoints
- **Earnings**: Earnings breakdown by period (today, week, month)
- **Completed Deliveries**: Total delivered orders count
- **Average Delivery Time**: Calculates average time between order and delivery using `deliveredDate`

### Technical Implementation
- **Complex Queries**: JPA `@Query` annotations with aggregation functions
- **Native Queries**: Used for time calculations (EXTRACT, EPOCH functions)
- **Date Range Filtering**: Flexible date range parameters for all analytics
- **Pagination**: Large result sets paginated for performance

---

## Phase 13: Rate Limiting

### What Was Done
- Implemented rate limiting using Bucket4j
- Configured different limits for different endpoint types (public, authenticated, payment)
- Created RateLimitInterceptor to intercept requests
- Added rate limit headers (X-RateLimit-*)
- Returns 429 (Too Many Requests) when limit exceeded

### Rate Limit Configuration
- **Public APIs**: 20 requests/minute (restaurant browsing, menu viewing)
- **Authenticated APIs**: 100 requests/minute (customer, restaurant, delivery endpoints)
- **Payment APIs**: 10 requests/minute (payment creation, verification)

### Technical Implementation
- **Bucket4j**: Token bucket algorithm for rate limiting
- **Caffeine Cache**: Stores rate limit buckets per user/IP
- **Interceptor**: `RateLimitInterceptor` checks limits before controllers
- **Headers**: Returns `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

### Why Rate Limiting
- **Prevent Abuse**: Protects against API abuse and DoS attacks
- **Resource Protection**: Prevents server overload
- **Fair Usage**: Ensures fair usage across all users
- **Payment Protection**: Extra protection for payment endpoints

---

## Phase 14: Swagger Documentation

### What Was Done
- Configured SpringDoc OpenAPI for Swagger UI
- Added `@Operation` annotations to all endpoints
- Documented request/response examples
- Added API descriptions and tags
- Configured JWT authentication in Swagger UI
- Created comprehensive API documentation

### Key Features
- **Interactive UI**: Swagger UI at `/swagger-ui.html` for testing endpoints
- **JWT Integration**: Authorize button in Swagger UI for authenticated endpoints
- **Request/Response Examples**: All endpoints have example request/response bodies
- **Parameter Documentation**: All parameters documented with descriptions
- **Error Responses**: Documented all possible error responses (400, 401, 403, 404, etc.)

### Documentation Annotations
- `@Tag`: Groups endpoints by functionality (Customer, Restaurant, Order, etc.)
- `@Operation`: Describes endpoint purpose and parameters
- `@ApiResponses`: Documents all possible HTTP responses
- `@Parameter`: Documents request parameters
- `@SecurityRequirement`: Marks endpoints requiring authentication

### Benefits
- **Developer Experience**: Easy API exploration and testing
- **Documentation**: Always up-to-date with code
- **Testing**: Test endpoints directly from Swagger UI
- **Onboarding**: New developers can quickly understand API structure

---

## Phase 15: Admin Module

### What Was Done
- Implemented user management (list all users, filter by role, activate/deactivate)
- Created restaurant approval workflow (list pending restaurants, approve/reject)
- Added order viewing for all orders (system-wide, filter by status)
- Integrated with existing analytics endpoints
- Added role-based access control (ADMIN role required)

### Admin Endpoints
- **User Management**:
  - `GET /api/admin/users`: List all users with pagination and role filtering
  - `PUT /api/admin/users/{id}/activate`: Activate or deactivate user accounts

- **Restaurant Management**:
  - `GET /api/admin/restaurants/pending-approval`: List restaurants awaiting approval
  - `PUT /api/admin/restaurants/{id}/approve`: Approve or reject restaurants

- **Order Management**:
  - `GET /api/admin/orders/all`: View all orders in system with status filtering

- **Analytics**: All analytics endpoints from Phase 12 accessible to admins

### Business Logic
- **User Activation**: Deactivated users cannot login
- **Restaurant Approval**: Only approved restaurants visible to customers and can receive orders
- **Order Viewing**: Admins can view all orders regardless of customer/restaurant
- **Role Filtering**: Filter users by role (CUSTOMER, RESTAURANT, DELIVERY, ADMIN)

### Security
- All admin endpoints require ADMIN role
- Protected by `@PreAuthorize("hasRole('ADMIN')")`
- JWT authentication required
- Only users with ADMIN role can access admin endpoints

---

### Technical Highlights
- **13 Entities** with proper relationships
- **22 Controllers** with RESTful endpoints
- **16 Services** implementing business logic
- **13 Repositories** with custom queries
- **35+ DTOs** for request/response handling
- **6 Configurations** for security, caching, documentation
- **Global Exception Handling** for consistent error responses

This project demonstrates a complete, production-ready Spring Boot application following industry best practices and design patterns.

