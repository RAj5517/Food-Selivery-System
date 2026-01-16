# Food Delivery System - Project Architecture

## Table of Contents
1. [Introduction](#introduction)
2. [Customer Entity Architecture](#customer-entity-architecture)
3. [Restaurant Entity Architecture](#restaurant-entity-architecture)
4. [Menu Entity Architecture](#menu-entity-architecture)
5. [Cart Entity Architecture](#cart-entity-architecture)
6. [Order Entity Architecture](#order-entity-architecture)
7. [Payment Entity Architecture](#payment-entity-architecture)
8. [Delivery Partner Entity Architecture](#delivery-partner-entity-architecture)
9. [Review Entity Architecture](#review-entity-architecture)
10. [Authentication Architecture](#authentication-architecture)
11. [Analytics Architecture](#analytics-architecture)
12. [Admin Architecture](#admin-architecture)
13. [Configuration Layer](#configuration-layer)
14. [Exception Handling](#exception-handling)
15. [Utility Classes](#utility-classes)

---

## Introduction

This document explains the **work and responsibilities of each file** in the Food Delivery System, organized by entity/functional area. Each section explains what controllers, services, and repositories do **without code details**, focusing on their purpose and responsibilities.

---

## Customer Entity Architecture

### Overview
Customer functionality includes profile management, address management, and cart/order operations.

### Files and Their Work

#### **CustomerController** (`controller/CustomerController.java`)
**Purpose**: Handles all customer-related HTTP requests

**Responsibilities**:
- Receives customer profile management requests
- Handles address management (add, list, update, delete)
- Validates customer-specific request DTOs
- Extracts authenticated customer information from security context
- Delegates business logic to CustomerService
- Returns customer profile and address responses
- Ensures only authenticated customers can access their own data

**Key Operations**:
- Get customer profile: Retrieves logged-in customer's profile information
- Update customer profile: Allows customers to update their name, profile image
- Add delivery address: Creates new delivery address for customer
- List addresses: Returns all delivery addresses for customer
- Set default address: Marks one address as default for delivery
- Delete address: Removes a delivery address

#### **CustomerService** (`service/CustomerService.java`)
**Purpose**: Implements all customer-related business logic

**Responsibilities**:
- Manages customer profile operations (get, update)
- Handles customer address CRUD operations
- Validates address uniqueness (one default address per customer)
- Ensures customer can only access their own data
- Manages wallet balance operations
- Converts between Customer entity and CustomerResponse DTO
- Converts between CustomerAddress entity and AddressResponse DTO
- Handles business rules (e.g., setting default address updates others)

**Key Operations**:
- Profile retrieval: Fetches customer data from database, converts to DTO
- Profile update: Updates customer name and profile image
- Address creation: Creates new address, handles default address logic
- Address listing: Retrieves all addresses for a customer
- Default address management: Ensures only one default address exists
- Address deletion: Removes address, updates default if deleted address was default

#### **CustomerRepository** (`repository/CustomerRepository.java`)
**Purpose**: Provides database access for Customer entity

**Responsibilities**:
- Provides CRUD operations for Customer entity
- Finds customer by user ID (for linking User and Customer)
- Extends JpaRepository for basic operations
- Uses Spring Data JPA for database queries

**Key Operations**:
- Find by user ID: Locates customer record associated with a user account

#### **CustomerAddressRepository** (`repository/CustomerAddressRepository.java`)
**Purpose**: Provides database access for CustomerAddress entity

**Responsibilities**:
- Provides CRUD operations for CustomerAddress entity
- Finds addresses by customer ID
- Finds default address for a customer
- Supports address management queries

**Key Operations**:
- Find by customer: Retrieves all addresses for a specific customer
- Find default address: Locates the default delivery address for a customer

#### **Model: Customer** (`model/Customer.java`)
**Purpose**: Represents customer profile in database

**What it represents**:
- Customer profile information (name, profile image)
- Wallet balance for future payments
- Links to User account (one-to-one relationship)
- Links to addresses, cart, orders, reviews (one-to-many relationships)

#### **Model: CustomerAddress** (`model/CustomerAddress.java`)
**Purpose**: Represents customer delivery addresses

**What it represents**:
- Delivery address details (street, city, pincode, coordinates)
- Address type (HOME, WORK, OTHER)
- Default address flag
- Links to Customer (many-to-one relationship)

#### **DTOs Related to Customer**
- **CustomerResponse**: Contains customer profile data for API responses
- **CustomerUpdateRequest**: Contains data for updating customer profile
- **AddressRequest**: Contains data for creating/updating addresses
- **AddressResponse**: Contains address data for API responses

---

## Restaurant Entity Architecture

### Overview
Restaurant functionality includes restaurant management, menu management, and restaurant operations.

### Files and Their Work

#### **RestaurantController** (`controller/RestaurantController.java`)
**Purpose**: Handles restaurant owner's restaurant management requests

**Responsibilities**:
- Receives restaurant profile management requests (restaurant owners only)
- Handles restaurant profile updates
- Allows restaurants to toggle open/closed status
- Validates restaurant-specific request DTOs
- Extracts authenticated restaurant user information
- Delegates business logic to RestaurantService
- Ensures only restaurant owners can manage their own restaurant

**Key Operations**:
- Get restaurant profile: Retrieves restaurant owner's restaurant details
- Update restaurant profile: Allows restaurant to update name, cuisine, address, coordinates
- Toggle restaurant status: Opens or closes restaurant for orders

#### **PublicRestaurantController** (`controller/PublicRestaurantController.java`)
**Purpose**: Handles public restaurant browsing requests (no authentication required)

**Responsibilities**:
- Receives public restaurant search/list requests
- Handles restaurant listing with filters (cuisine, city, rating)
- Supports pagination and sorting
- Supports nearby restaurant search using coordinates
- Returns restaurant information to public users
- No authentication required for these endpoints

**Key Operations**:
- List all restaurants: Returns paginated list with filtering options
- Get restaurant by ID: Returns detailed restaurant information
- Get restaurants by cuisine: Filters restaurants by cuisine type
- Get restaurants by city: Filters restaurants by city location
- Get nearby restaurants: Finds restaurants within radius using coordinates

#### **RestaurantService** (`service/RestaurantService.java`)
**Purpose**: Implements all restaurant-related business logic

**Responsibilities**:
- Manages restaurant CRUD operations
- Implements restaurant listing with complex filters
- Calculates nearby restaurants using Haversine formula (distance calculation)
- Manages restaurant rating calculations
- Handles restaurant approval status
- Caches restaurant data for performance
- Converts between Restaurant entity and RestaurantResponse DTO
- Manages cache eviction on updates

**Key Operations**:
- Restaurant listing: Complex query filtering by cuisine, city, rating, approval status
- Nearby search: Geographic distance calculation for location-based search
- Restaurant retrieval: Gets restaurant by ID with caching
- Restaurant update: Updates restaurant details, evicts cache
- Rating calculation: Updates restaurant rating from reviews

#### **RestaurantRepository** (`repository/RestaurantRepository.java`)
**Purpose**: Provides database access for Restaurant entity

**Responsibilities**:
- Provides CRUD operations for Restaurant entity
- Implements custom queries for filtering (cuisine, city, rating)
- Implements geographic queries for nearby restaurant search
- Supports pagination for all queries
- Finds restaurant by user ID (for restaurant owner access)
- Finds restaurants by approval status (for admin approval workflow)

**Key Operations**:
- Find by user ID: Locates restaurant associated with user account
- Find by cuisine: Filters restaurants by cuisine type
- Find by city: Searches restaurants by city in address
- Find by rating: Filters restaurants with minimum rating
- Find nearby: Complex query calculating distance using lat/long
- Find by approval status: Used by admin to find pending restaurants

#### **Model: Restaurant** (`model/Restaurant.java`)
**Purpose**: Represents restaurant in database

**What it represents**:
- Restaurant information (name, cuisine, address, coordinates)
- Operational status (isOpen, isApproved)
- Rating and average preparation time
- Links to User account (one-to-one)
- Links to menu items, orders, reviews (one-to-many)

---

## Menu Entity Architecture

### Overview
Menu functionality includes menu item management for restaurants.

### Files and Their Work

#### **MenuController** (`controller/MenuController.java`)
**Purpose**: Handles restaurant owner's menu management requests

**Responsibilities**:
- Receives menu item management requests (restaurant owners only)
- Handles menu item CRUD operations
- Validates menu item request DTOs
- Extracts authenticated restaurant user information
- Delegates to MenuService
- Ensures only restaurant owners can manage their own menu

**Key Operations**:
- Add menu item: Creates new menu item for restaurant
- Update menu item: Updates existing menu item details
- Delete menu item: Removes menu item from menu
- Toggle availability: Enables/disables menu item availability

#### **PublicMenuController** (`controller/PublicMenuController.java`)
**Purpose**: Handles public menu viewing requests (no authentication required)

**Responsibilities**:
- Receives public menu viewing requests
- Handles menu listing with filters (category, vegetarian, availability)
- Supports pagination
- Returns menu information to public users
- No authentication required

**Key Operations**:
- Get restaurant menu: Returns all menu items for a restaurant
- Get menu item by ID: Returns detailed menu item information

#### **MenuService** (`service/MenuService.java`)
**Purpose**: Implements all menu-related business logic

**Responsibilities**:
- Manages menu item CRUD operations
- Validates menu item ownership (restaurant can only manage own items)
- Manages menu item availability
- Caches menu items for performance
- Converts between MenuItem entity and MenuItemResponse DTO
- Handles cache eviction on menu updates

**Key Operations**:
- Menu item creation: Creates new menu item, validates restaurant ownership
- Menu item update: Updates menu item details, evicts cache
- Menu item deletion: Removes menu item, updates cache
- Menu retrieval: Gets menu items with filtering and caching

#### **MenuItemRepository** (`repository/MenuItemRepository.java`)
**Purpose**: Provides database access for MenuItem entity

**Responsibilities**:
- Provides CRUD operations for MenuItem entity
- Finds menu items by restaurant ID
- Finds menu items by category
- Supports filtering by availability and vegetarian options
- Supports pagination

**Key Operations**:
- Find by restaurant: Retrieves all menu items for a restaurant
- Find by category: Filters menu items by category
- Find available items: Filters only available menu items

#### **Model: MenuItem** (`model/MenuItem.java`)
**Purpose**: Represents menu item in database

**What it represents**:
- Menu item details (name, price, description, image)
- Item properties (isVeg, isAvailable)
- Links to Restaurant and Category (many-to-one relationships)

#### **Model: Category** (`model/Category.java`)
**Purpose**: Represents food category

**What it represents**:
- Category name and description
- Links to menu items (one-to-many relationship)

---

## Cart Entity Architecture

### Overview
Cart functionality includes shopping cart management for customers.

### Files and Their Work

#### **CartController** (`controller/CartController.java`)
**Purpose**: Handles customer shopping cart requests

**Responsibilities**:
- Receives cart management requests (customers only)
- Handles adding items to cart
- Handles updating item quantities
- Handles removing items from cart
- Handles clearing entire cart
- Validates cart item request DTOs
- Extracts authenticated customer information
- Delegates to CartService
- Returns cart response with items and total

**Key Operations**:
- Get cart: Retrieves current customer's shopping cart
- Add item to cart: Adds menu item to cart or updates quantity if exists
- Update quantity: Changes quantity of cart item
- Remove item: Removes specific item from cart
- Clear cart: Removes all items from cart

#### **CartService** (`service/CartService.java`)
**Purpose**: Implements all cart-related business logic

**Responsibilities**:
- Manages cart creation (creates cart if customer doesn't have one)
- Handles adding items to cart (creates or updates cart items)
- Validates cart operations (same restaurant constraint, item availability)
- Calculates cart totals
- Manages cart-item relationships
- Converts between Cart/CartItem entities and CartResponse/CartItemResponse DTOs
- Ensures cart can only contain items from one restaurant

**Key Operations**:
- Cart retrieval: Gets or creates customer's cart
- Item addition: Adds menu item to cart, validates restaurant, updates quantity if exists
- Quantity update: Updates cart item quantity, recalculates totals
- Item removal: Removes cart item, updates cart totals
- Cart clearing: Removes all items, resets cart
- Total calculation: Calculates cart total from all items

#### **CartRepository** (`repository/CartRepository.java`)
**Purpose**: Provides database access for Cart entity

**Responsibilities**:
- Provides CRUD operations for Cart entity
- Finds cart by customer ID
- Supports cart retrieval for customers

**Key Operations**:
- Find by customer: Locates customer's active shopping cart

#### **CartItemRepository** (`repository/CartItemRepository.java`)
**Purpose**: Provides database access for CartItem entity

**Responsibilities**:
- Provides CRUD operations for CartItem entity
- Finds cart items by cart ID
- Supports cart item management

**Key Operations**:
- Find by cart: Retrieves all items in a specific cart

#### **Model: Cart** (`model/Cart.java`)
**Purpose**: Represents shopping cart in database

**What it represents**:
- Cart information (customer, restaurant, creation date)
- Links to Customer and Restaurant (many-to-one)
- Links to cart items (one-to-many relationship)

#### **Model: CartItem** (`model/CartItem.java`)
**Purpose**: Represents item in shopping cart

**What it represents**:
- Cart item details (menu item reference, quantity, price at time of addition)
- Links to Cart and MenuItem (many-to-one relationships)

---

## Order Entity Architecture

### Overview
Order functionality includes order placement, tracking, and management for customers, restaurants, and delivery partners.

### Files and Their Work

#### **OrderController** (`controller/OrderController.java`)
**Purpose**: Handles customer order requests

**Responsibilities**:
- Receives customer order management requests
- Handles order placement from cart
- Handles order retrieval by ID
- Handles order listing for customer
- Handles order cancellation
- Validates order request DTOs
- Extracts authenticated customer information
- Delegates to OrderService
- Returns order responses with full details

**Key Operations**:
- Place order: Creates order from customer's cart
- Get order by ID: Retrieves detailed order information
- Get customer orders: Lists customer's orders with pagination and status filtering
- Cancel order: Cancels order if in cancellable state

#### **RestaurantOrderController** (`controller/RestaurantOrderController.java`)
**Purpose**: Handles restaurant's order management requests

**Responsibilities**:
- Receives restaurant order management requests (restaurant owners only)
- Handles order status updates (confirm, preparing, ready)
- Lists orders assigned to restaurant
- Filters orders by status
- Extracts authenticated restaurant information
- Delegates to OrderService
- Ensures restaurants can only manage their own orders

**Key Operations**:
- Confirm order: Restaurant accepts and confirms order
- Mark preparing: Updates order status to preparing
- Mark ready: Updates order status to ready for pickup
- Get restaurant orders: Lists all orders for restaurant with filtering

#### **DeliveryOrderController** (`controller/DeliveryOrderController.java`)
**Purpose**: Handles delivery partner's order requests

**Responsibilities**:
- Receives delivery partner order requests (delivery partners only)
- Handles viewing available orders for pickup
- Handles accepting delivery assignments
- Handles marking orders as delivered
- Extracts authenticated delivery partner information
- Delegates to OrderService
- Ensures delivery partners can only manage assigned orders

**Key Operations**:
- Get available orders: Lists orders ready for delivery pickup
- Accept order: Assigns order to delivery partner
- Mark delivered: Updates order status to delivered, sets delivery time

#### **OrderService** (`service/OrderService.java`)
**Purpose**: Implements all order-related business logic

**Responsibilities**:
- Manages order lifecycle (state machine: PENDING → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED)
- Handles order placement from cart (creates order, clears cart)
- Validates order operations (cart not empty, items available, address valid)
- Manages order status transitions with validation
- Calculates order totals
- Manages order-item relationships
- Sends email notifications on order status changes
- Converts between Order/OrderItem entities and OrderResponse/OrderItemResponse DTOs
- Handles order cancellation logic

**Key Operations**:
- Order placement: Creates order from cart, validates cart, creates order items, clears cart
- Order status updates: Validates state transitions, updates order status, sends notifications
- Order retrieval: Gets order with all relationships loaded
- Order cancellation: Validates cancellable state, cancels order, handles refunds if needed
- Order assignment: Assigns delivery partner to order, updates status
- Order delivery: Marks order as delivered, sets delivery timestamp

#### **OrderRepository** (`repository/OrderRepository.java`)
**Purpose**: Provides database access for Order entity

**Responsibilities**:
- Provides CRUD operations for Order entity
- Finds orders by customer, restaurant, delivery partner
- Finds orders by status
- Finds orders by date range
- Implements complex queries for analytics (revenue, trends)
- Supports pagination for all queries
- Calculates aggregations (sum, count, average) for analytics

**Key Operations**:
- Find by customer: Retrieves orders for a specific customer
- Find by restaurant: Retrieves orders for a specific restaurant
- Find by delivery partner: Retrieves orders assigned to delivery partner
- Find by status: Filters orders by order status
- Find by date range: Retrieves orders within date range
- Analytics queries: Complex aggregations for revenue and trend analysis

#### **OrderItemRepository** (`repository/OrderItemRepository.java`)
**Purpose**: Provides database access for OrderItem entity

**Responsibilities**:
- Provides CRUD operations for OrderItem entity
- Finds order items by order ID
- Supports order item retrieval

**Key Operations**:
- Find by order: Retrieves all items in a specific order

#### **Model: Order** (`model/Order.java`)
**Purpose**: Represents order in database

**What it represents**:
- Order information (status, total amount, payment status, dates)
- Links to Customer, Restaurant, DeliveryPartner, Address (many-to-one)
- Links to order items (one-to-many)
- Links to Payment and Review (one-to-one)

**Order Status Flow**:
```
PENDING → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED
         ↓
      CANCELLED
```

#### **Model: OrderItem** (`model/OrderItem.java`)
**Purpose**: Represents item in an order

**What it represents**:
- Order item details (menu item reference, quantity, price at time of order)
- Links to Order and MenuItem (many-to-one relationships)

---

## Payment Entity Architecture

### Overview
Payment functionality includes payment processing using Razorpay payment gateway.

### Files and Their Work

#### **PaymentController** (`controller/PaymentController.java`)
**Purpose**: Handles payment-related requests

**Responsibilities**:
- Receives payment creation requests
- Handles payment verification
- Handles refund processing
- Handles payment retrieval by order ID
- Validates payment request DTOs
- Extracts authenticated customer information
- Delegates to PaymentService
- Returns payment responses with transaction details
- Rate limited to 10 requests/minute

**Key Operations**:
- Create payment order: Creates Razorpay payment order
- Verify payment: Verifies payment signature from Razorpay
- Process refund: Initiates refund for an order
- Get payment by order: Retrieves payment details for an order

#### **PaymentService** (`service/PaymentService.java`)
**Purpose**: Implements all payment-related business logic

**Responsibilities**:
- Integrates with Razorpay payment gateway
- Creates payment orders in Razorpay
- Verifies payment signatures for security
- Processes refunds through Razorpay
- Updates order payment status
- Stores payment records in database
- Validates payment operations (order must exist, amount must match)
- Converts between Payment entity and PaymentResponse DTO
- Handles payment failures and errors

**Key Operations**:
- Payment creation: Creates order in Razorpay, stores payment record
- Payment verification: Validates Razorpay signature, updates payment and order status
- Refund processing: Initiates refund in Razorpay, updates payment status
- Payment retrieval: Gets payment details for an order

#### **PaymentRepository** (`repository/PaymentRepository.java`)
**Purpose**: Provides database access for Payment entity

**Responsibilities**:
- Provides CRUD operations for Payment entity
- Finds payment by order ID
- Supports payment history queries
- Finds payments by status

**Key Operations**:
- Find by order: Locates payment record for an order
- Find by status: Filters payments by payment status

#### **Model: Payment** (`model/Payment.java`)
**Purpose**: Represents payment in database

**What it represents**:
- Payment information (amount, method, status, transaction ID)
- Links to Order (one-to-one relationship)
- Payment date and status tracking

#### **RazorpayConfig** (`config/RazorpayConfig.java`)
**Purpose**: Configures Razorpay payment gateway

**Responsibilities**:
- Initializes Razorpay client with API keys
- Provides Razorpay client bean for dependency injection
- Manages payment gateway configuration

---

## Delivery Partner Entity Architecture

### Overview
Delivery partner functionality includes delivery partner profile management and order delivery operations.

### Files and Their Work

#### **DeliveryPartnerController** (`controller/DeliveryPartnerController.java`)
**Purpose**: Handles delivery partner profile requests

**Responsibilities**:
- Receives delivery partner profile management requests
- Handles profile updates
- Handles availability toggling (online/offline)
- Handles location updates
- Extracts authenticated delivery partner information
- Delegates to DeliveryPartnerService
- Ensures only delivery partners can manage their own profile

**Key Operations**:
- Get profile: Retrieves delivery partner profile
- Update profile: Updates delivery partner information
- Toggle availability: Sets online/offline status
- Update location: Updates current latitude/longitude

#### **DeliveryPartnerService** (`service/DeliveryPartnerService.java`)
**Purpose**: Implements delivery partner business logic

**Responsibilities**:
- Manages delivery partner profile operations
- Handles availability status updates
- Manages location tracking
- Converts between DeliveryPartner entity and DeliveryPartnerResponse DTO

**Key Operations**:
- Profile management: Get and update delivery partner profile
- Availability management: Update online/offline status
- Location tracking: Update current coordinates for order assignment

#### **DeliveryPartnerRepository** (`repository/DeliveryPartnerRepository.java`)
**Purpose**: Provides database access for DeliveryPartner entity

**Responsibilities**:
- Provides CRUD operations for DeliveryPartner entity
- Finds delivery partner by user ID
- Finds available delivery partners
- Supports location-based queries

**Key Operations**:
- Find by user ID: Locates delivery partner associated with user account
- Find available partners: Lists delivery partners currently online

#### **Model: DeliveryPartner** (`model/DeliveryPartner.java`)
**Purpose**: Represents delivery partner in database

**What it represents**:
- Delivery partner information (name, vehicle type, availability)
- Current location (latitude, longitude)
- Links to User account (one-to-one)
- Links to orders (one-to-many)

---

## Review Entity Architecture

### Overview
Review functionality includes customer reviews and ratings for restaurants.

### Files and Their Work

#### **ReviewService** (`service/ReviewService.java`)
**Purpose**: Implements review-related business logic

**Responsibilities**:
- Manages review submission after order delivery
- Validates review operations (one review per order, order must be delivered)
- Calculates and updates restaurant average ratings
- Converts between Review entity and ReviewResponse DTO
- Retrieves reviews for restaurants

**Key Operations**:
- Submit review: Creates review for delivered order, updates restaurant rating
- Get restaurant reviews: Retrieves all reviews for a restaurant
- Rating calculation: Updates restaurant average rating from reviews

#### **ReviewRepository** (`repository/ReviewRepository.java`)
**Purpose**: Provides database access for Review entity

**Responsibilities**:
- Provides CRUD operations for Review entity
- Finds reviews by restaurant ID
- Finds reviews by customer ID
- Finds review by order ID
- Calculates average rating for restaurant

**Key Operations**:
- Find by restaurant: Retrieves all reviews for a restaurant
- Find by customer: Retrieves customer's reviews
- Find by order: Locates review for specific order
- Calculate average rating: Computes average rating using aggregation query

#### **Model: Review** (`model/Review.java`)
**Purpose**: Represents review in database

**What it represents**:
- Review information (rating, comment, creation date)
- Links to Order, Customer, Restaurant (many-to-one relationships)

---

## Authentication Architecture

### Overview
Authentication functionality includes user registration, login, and JWT-based security.

### Files and Their Work

#### **AuthController** (`controller/AuthController.java`)
**Purpose**: Handles authentication-related requests (public endpoints)

**Responsibilities**:
- Receives user registration requests (customer, restaurant, delivery)
- Receives login requests
- Validates registration and login DTOs
- Delegates to AuthService
- Returns authentication responses with JWT tokens
- Public endpoints (no authentication required)

**Key Operations**:
- Register customer: Creates new customer account
- Register restaurant: Creates new restaurant account (pending approval)
- Register delivery: Creates new delivery partner account
- Login: Authenticates user and returns JWT token

#### **AuthService** (`service/AuthService.java`)
**Purpose**: Implements authentication business logic

**Responsibilities**:
- Handles user registration for all roles
- Creates User entity and role-specific entities (Customer, Restaurant, DeliveryPartner)
- Validates email uniqueness
- Hashes passwords using BCrypt
- Generates JWT tokens
- Authenticates users during login
- Sends registration confirmation emails
- Manages user activation status

**Key Operations**:
- Customer registration: Creates user and customer entities, generates token, sends email
- Restaurant registration: Creates user and restaurant entities (isApproved=false), generates token
- Delivery registration: Creates user and delivery partner entities, generates token
- Login: Validates credentials, generates and returns JWT token

#### **JwtAuthenticationFilter** (`security/JwtAuthenticationFilter.java`)
**Purpose**: Intercepts HTTP requests to validate JWT tokens

**Responsibilities**:
- Intercepts all incoming HTTP requests (except public endpoints)
- Extracts JWT token from Authorization header
- Validates token signature and expiration
- Loads user details from database if token valid
- Sets Spring Security context with user authentication
- Allows request to proceed if valid, rejects if invalid

**Key Operations**:
- Token extraction: Reads "Bearer <token>" from Authorization header
- Token validation: Validates signature, expiration, user existence
- Authentication setting: Sets security context for authorized requests

#### **CustomUserDetailsService** (`service/CustomUserDetailsService.java`)
**Purpose**: Loads user details for Spring Security

**Responsibilities**:
- Implements Spring's UserDetailsService interface
- Loads user from database by email
- Converts User entity to Spring Security UserDetails
- Maps user roles to Spring Security authorities

**Key Operations**:
- Load user by username: Finds user by email, converts to UserDetails

#### **JwtUtil** (`util/JwtUtil.java`)
**Purpose**: Provides JWT token utility methods

**Responsibilities**:
- Generates JWT tokens with user email and role
- Validates JWT token signatures
- Extracts information from tokens (username, role, expiration)
- Manages token expiration

**Key Operations**:
- Token generation: Creates signed JWT token with user claims
- Token validation: Verifies token signature and expiration
- Token extraction: Extracts username and role from token

#### **SecurityUtil** (`util/SecurityUtil.java`)
**Purpose**: Provides utility methods for security context access

**Responsibilities**:
- Extracts user ID from Spring Security context
- Provides helper methods for accessing authenticated user information

**Key Operations**:
- Get user ID: Extracts authenticated user's ID from security context

---

## Analytics Architecture

### Overview
Analytics functionality provides insights for admin, restaurants, and delivery partners.

### Files and Their Work

#### **AdminAnalyticsController** (`controller/AdminAnalyticsController.java`)
**Purpose**: Handles admin analytics requests (admin only)

**Responsibilities**:
- Receives system-wide analytics requests
- Handles overview statistics requests
- Handles order trend analysis requests
- Handles revenue analysis requests
- Handles top restaurants/customers requests
- Extracts authenticated admin information
- Delegates to AdminAnalyticsService
- Returns analytics data with aggregations

**Key Operations**:
- Get overview: System-wide statistics (total orders, revenue, users)
- Get order trends: Order trends by period (daily, weekly, monthly)
- Get revenue: Revenue analysis with date range filtering
- Get top restaurants: Most popular restaurants by order count
- Get top customers: Most active customers by order count

#### **AdminAnalyticsService** (`service/AdminAnalyticsService.java`)
**Purpose**: Implements admin analytics business logic

**Responsibilities**:
- Calculates system-wide statistics
- Performs complex aggregations (sum, count, average)
- Analyzes order trends over time
- Identifies top-performing restaurants and customers
- Converts raw query results to analytics DTOs
- Handles date range calculations for trends

**Key Operations**:
- Overview calculation: Aggregates total orders, revenue, users, restaurants
- Trend analysis: Groups orders by time period, calculates counts and revenue
- Top performers: Identifies restaurants and customers with most orders/revenue

#### **RestaurantAnalyticsController** (`controller/RestaurantAnalyticsController.java`)
**Purpose**: Handles restaurant analytics requests (restaurant owners only)

**Responsibilities**:
- Receives restaurant-specific analytics requests
- Handles dashboard statistics requests
- Handles popular items requests
- Handles revenue breakdown requests
- Handles peak hours analysis requests
- Extracts authenticated restaurant information
- Delegates to RestaurantAnalyticsService
- Returns restaurant analytics data

**Key Operations**:
- Get dashboard: Today's orders, revenue, average rating
- Get popular items: Most ordered menu items
- Get revenue: Revenue breakdown by date range
- Get peak hours: Busiest hours for orders

#### **RestaurantAnalyticsService** (`service/RestaurantAnalyticsService.java`)
**Purpose**: Implements restaurant analytics business logic

**Responsibilities**:
- Calculates restaurant-specific statistics
- Analyzes menu item popularity
- Performs revenue breakdowns
- Analyzes peak ordering hours
- Uses ReviewRepository for rating calculations
- Converts query results to analytics DTOs

**Key Operations**:
- Dashboard calculation: Aggregates today's orders and revenue
- Popular items: Groups order items, counts orders and quantities
- Revenue analysis: Sums revenue by date range
- Peak hours: Groups orders by hour, counts order frequency

#### **DeliveryAnalyticsController** (`controller/DeliveryAnalyticsController.java`)
**Purpose**: Handles delivery partner analytics requests (delivery partners only)

**Responsibilities**:
- Receives delivery partner-specific analytics requests
- Handles earnings requests
- Handles completed deliveries requests
- Handles average delivery time requests
- Extracts authenticated delivery partner information
- Delegates to DeliveryAnalyticsService
- Returns delivery analytics data

**Key Operations**:
- Get earnings: Earnings breakdown by period
- Get completed deliveries: Total completed deliveries count
- Get average delivery time: Calculates average delivery time in minutes

#### **DeliveryAnalyticsService** (`service/DeliveryAnalyticsService.java`)
**Purpose**: Implements delivery partner analytics business logic

**Responsibilities**:
- Calculates delivery partner-specific statistics
- Analyzes earnings by time period
- Counts completed deliveries
- Calculates average delivery time (using deliveredDate - orderDate)
- Uses native SQL queries for time calculations
- Converts query results to analytics DTOs

**Key Operations**:
- Earnings calculation: Sums delivery fees by period
- Delivery count: Counts delivered orders
- Delivery time: Calculates average time between order and delivery

---

## Admin Architecture

### Overview
Admin functionality provides administrative control over the entire platform.

### Files and Their Work

#### **AdminUserController** (`controller/AdminUserController.java`)
**Purpose**: Handles user management requests (admin only)

**Responsibilities**:
- Receives user management requests
- Handles user listing with role filtering
- Handles user activation/deactivation
- Extracts authenticated admin information
- Delegates to AdminService
- Returns user information for admin

**Key Operations**:
- List users: Retrieves all users with optional role filter
- Activate/deactivate user: Updates user active status

#### **AdminRestaurantController** (`controller/AdminRestaurantController.java`)
**Purpose**: Handles restaurant approval requests (admin only)

**Responsibilities**:
- Receives restaurant approval requests
- Handles pending restaurants listing
- Handles restaurant approval/rejection
- Extracts authenticated admin information
- Delegates to AdminService
- Returns restaurant information for admin

**Key Operations**:
- Get pending restaurants: Lists restaurants awaiting approval
- Approve/reject restaurant: Updates restaurant approval status

#### **AdminOrderController** (`controller/AdminOrderController.java`)
**Purpose**: Handles order viewing requests (admin only)

**Responsibilities**:
- Receives order viewing requests
- Handles all orders listing with status filtering
- Extracts authenticated admin information
- Delegates to AdminService
- Returns order information for admin

**Key Operations**:
- Get all orders: Lists all orders in system with optional status filter

#### **AdminService** (`service/AdminService.java`)
**Purpose**: Implements admin business logic

**Responsibilities**:
- Manages user listing and filtering
- Handles user activation/deactivation
- Manages restaurant approval workflow
- Handles order viewing for all customers
- Converts entities to admin response DTOs
- Coordinates between multiple repositories

**Key Operations**:
- User management: List users, activate/deactivate
- Restaurant approval: List pending restaurants, approve/reject
- Order viewing: List all orders with filtering

---

## Configuration Layer

### Overview
Configuration classes set up application-wide settings, security, caching, and integrations.

### Files and Their Work

#### **SecurityConfig** (`config/SecurityConfig.java`)
**Purpose**: Configures Spring Security for the application

**Responsibilities**:
- Configures security filter chain
- Defines public vs protected endpoints
- Sets up role-based access control
- Configures CORS (Cross-Origin Resource Sharing)
- Configures password encoder (BCrypt)
- Sets up authentication manager
- Registers JWT authentication filter
- Defines authentication provider

**Key Configurations**:
- Public endpoints: `/api/auth/**`, `/api/restaurants/**`, Swagger endpoints
- Protected endpoints: Role-based access (`/api/admin/**` requires ADMIN, etc.)
- JWT filter: Positioned before username/password authentication filter
- Password encoding: BCrypt with strength 10

#### **CacheConfig** (`config/CacheConfig.java`)
**Purpose**: Configures caching for the application

**Responsibilities**:
- Configures Caffeine cache manager
- Sets cache specifications (size, expiration)
- Defines cache names (restaurants, menuItems)
- Registers cache manager bean

**Key Configurations**:
- Cache type: Caffeine (in-memory)
- Maximum size: 500 entries per cache
- Expiration: 30 minutes after write

#### **OpenApiConfig** (`config/OpenApiConfig.java`)
**Purpose**: Configures Swagger/OpenAPI documentation

**Responsibilities**:
- Sets up OpenAPI documentation
- Configures API information (title, description, version)
- Sets up JWT authentication in Swagger UI
- Configures API server URLs
- Defines security schemes

**Key Configurations**:
- API title and description
- JWT bearer authentication scheme
- Server URLs (development, production)
- Security requirements for protected endpoints

#### **RazorpayConfig** (`config/RazorpayConfig.java`)
**Purpose**: Configures Razorpay payment gateway

**Responsibilities**:
- Initializes Razorpay client
- Configures API keys (key ID, secret)
- Provides Razorpay client bean

**Key Configurations**:
- Razorpay API keys from properties
- Currency setting (INR)

#### **WebMvcConfig** (`config/WebMvcConfig.java`)
**Purpose**: Configures Spring MVC settings

**Responsibilities**:
- Registers interceptors (rate limiting)
- Configures path patterns for interceptors
- Sets up CORS configuration if needed

**Key Configurations**:
- Rate limit interceptor registration
- Interceptor path patterns (`/api/**`)

#### **RateLimitInterceptor** (`config/RateLimitInterceptor.java`)
**Purpose**: Intercepts requests to enforce rate limiting

**Responsibilities**:
- Intercepts HTTP requests before controllers
- Determines rate limit type based on endpoint (public, authenticated, payment)
- Identifies user (by username for authenticated, by IP for public)
- Checks rate limits using RateLimitService
- Sets rate limit headers (X-RateLimit-*)
- Returns 429 if rate limit exceeded

**Key Operations**:
- Request interception: Runs before controller methods
- Key identification: Uses username or IP address
- Limit checking: Queries RateLimitService for allowance
- Header setting: Adds rate limit headers to responses

#### **RateLimitService** (`service/RateLimitService.java`)
**Purpose**: Manages rate limit buckets using Bucket4j

**Responsibilities**:
- Creates and manages rate limit buckets using Bucket4j
- Stores buckets in Caffeine cache
- Supports different limit types (PUBLIC, AUTHENTICATED, PAYMENT)
- Consumes tokens from buckets
- Returns remaining tokens
- Configures rate limits from properties (20/min, 100/min, 10/min)

**Key Operations**:
- Bucket creation: Creates Bucket4j buckets for each user/IP
- Token consumption: Tries to consume token, returns true/false
- Token availability: Returns remaining tokens in bucket

---

## Exception Handling

### Overview
Exception handling provides centralized error management across the application.

### Files and Their Work

#### **GlobalExceptionHandler** (`exception/GlobalExceptionHandler.java`)
**Purpose**: Centralized exception handling for the entire application

**Responsibilities**:
- Catches all exceptions thrown by controllers/services
- Converts exceptions to appropriate HTTP responses
- Provides consistent error response format
- Handles validation errors from DTOs
- Maps exceptions to HTTP status codes
- Includes error details (status, message, timestamp, path)

**Exception Handling**:
- **ResourceNotFoundException** → 404 Not Found
- **UnauthorizedException** → 401 Unauthorized
- **BadRequestException** → 400 Bad Request
- **ConflictException** → 409 Conflict
- **MethodArgumentNotValidException** → 400 with validation details
- **General Exception** → 500 Internal Server Error

**Custom Exceptions**:
- **ResourceNotFoundException**: Entity not found in database
- **UnauthorizedException**: Authentication/authorization failure
- **BadRequestException**: Invalid request data
- **ConflictException**: Resource conflict (e.g., duplicate email)

---

## Utility Classes

### Overview
Utility classes provide reusable helper methods across the application.

### Files and Their Work

#### **JwtUtil** (`util/JwtUtil.java`)
**Purpose**: JWT token utility methods

**Responsibilities**:
- Generates JWT tokens with claims (email, role)
- Validates JWT token signatures
- Extracts information from tokens (username, role, expiration)
- Manages token expiration time

#### **SecurityUtil** (`util/SecurityUtil.java`)
**Purpose**: Security context utility methods

**Responsibilities**:
- Extracts user ID from Spring Security context
- Provides helper methods for accessing authenticated user information
- Used by services to get current user ID

#### **FileUploadUtil** (`util/FileUploadUtil.java`)
**Purpose**: File upload utility methods

**Responsibilities**:
- Validates uploaded files (type, size)
- Saves files to local filesystem or cloud storage
- Generates unique file names
- Returns file URLs for responses
- Used for restaurant images and menu item images

---

## Summary

This architecture provides:

1. **Clear Separation**: Controller → Service → Repository layers
2. **Single Responsibility**: Each file has a specific purpose
3. **Business Logic Centralization**: All logic in services, not controllers
4. **Data Access Abstraction**: Repositories abstract database operations
5. **Security**: Authentication and authorization at multiple layers
6. **Error Handling**: Centralized exception handling
7. **Performance**: Caching and rate limiting for optimization
8. **Documentation**: Swagger annotations for API documentation
9. **Maintainability**: Well-organized structure for easy understanding and modification

Each entity (Customer, Restaurant, Order, etc.) has its own complete set of Controller, Service, Repository, and Model files working together to provide end-to-end functionality.

